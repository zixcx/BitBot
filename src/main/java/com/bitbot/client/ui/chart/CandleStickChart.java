package com.bitbot.client.ui.chart;

import com.bitbot.client.model.Candle;
import com.bitbot.client.service.ThemeManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom CandleStick Chart Component
 * Features:
 * - Renders OHLCV candles
 * - Zoom and Pan support (future)
 * - Trade markers (Buy/Sell arrows)
 * - Volume bars at bottom
 * - Theme support (Light/Dark mode)
 */
public class CandleStickChart extends Pane {
    
    private static final Color BULLISH_COLOR = Color.web(ThemeManager.COLOR_SUCCESS);
    private static final Color BEARISH_COLOR = Color.web(ThemeManager.COLOR_ERROR);
    
    private static final double CHART_PADDING = 60;
    private static final double VOLUME_HEIGHT_RATIO = 0.2;
    private static final double CANDLE_WIDTH_RATIO = 0.7;
    
    private final Canvas canvas;
    private final ThemeManager themeManager;
    private List<Candle> candles;
    private double minPrice = Double.MAX_VALUE;
    private double maxPrice = Double.MIN_VALUE;
    private double maxVolume = 0;

    public CandleStickChart() {
        this.themeManager = ThemeManager.getInstance();
        canvas = new Canvas();
        getChildren().add(canvas);
        
        candles = new ArrayList<>();
        
        // Bind canvas size to pane size
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        
        // Redraw when size changes
        widthProperty().addListener((obs, old, newVal) -> draw());
        heightProperty().addListener((obs, old, newVal) -> draw());
        
        // Redraw when theme changes
        themeManager.darkModeProperty().addListener((obs, old, newVal) -> draw());
    }

    /**
     * Set candle data and redraw
     */
    public void setData(List<Candle> candles) {
        this.candles = new ArrayList<>(candles);
        calculateBounds();
        draw();
    }

    /**
     * Add a single candle and redraw
     */
    public void addCandle(Candle candle) {
        candles.add(candle);
        calculateBounds();
        draw();
    }

    /**
     * Calculate min/max bounds for scaling
     * Uses IQR (Interquartile Range) method to filter out extreme outliers
     * and calculates reasonable bounds for the chart
     */
    private void calculateBounds() {
        if (candles.isEmpty()) {
            minPrice = 0;
            maxPrice = 100;
            maxVolume = 0;
            return;
        }

        // 1. Calculate Max Volume (Consider all candles)
        maxVolume = 0;
        for (Candle candle : candles) {
            maxVolume = Math.max(maxVolume, candle.volume());
        }

        // 2. Collect all valid high/low prices for outlier detection
        List<Double> prices = new ArrayList<>();
        for (Candle candle : candles) {
            if (candle.high() > 0) prices.add(candle.high());
            if (candle.low() > 0) prices.add(candle.low());
        }
        
        if (prices.isEmpty()) {
            minPrice = 0;
            maxPrice = 100;
            return;
        }

        // Sort prices to find quartiles
        prices.sort(Double::compareTo);
        
        double q1 = prices.get(prices.size() / 4);
        double q3 = prices.get(prices.size() * 3 / 4);
        double iqr = q3 - q1;
        
        // Define bounds for outliers (Using 3.0 * IQR for extreme outliers)
        double lowerBound = q1 - 3.0 * iqr;
        double upperBound = q3 + 3.0 * iqr;
        
        minPrice = Double.MAX_VALUE;
        maxPrice = Double.MIN_VALUE;
        
        // 3. Calculate min/max price excluding outliers
        for (Candle candle : candles) {
            // Skip extreme outliers for scaling calculation
            if (candle.low() < lowerBound || candle.high() > upperBound) {
                continue;
            }
            
            if (candle.low() > 0) {
                minPrice = Math.min(minPrice, candle.low());
            }
            if (candle.high() > 0) {
                maxPrice = Math.max(maxPrice, candle.high());
            }
        }
        
        // Fallback if all data was filtered out or invalid range
        if (minPrice == Double.MAX_VALUE || maxPrice == Double.MIN_VALUE) {
            minPrice = q1 * 0.9;
            maxPrice = q3 * 1.1;
        }
        
        // Add 5% padding to price range
        double priceRange = maxPrice - minPrice;
        if (priceRange == 0) {
            minPrice -= maxPrice * 0.05;
            maxPrice += maxPrice * 0.05;
        } else {
            minPrice -= priceRange * 0.05;
            maxPrice += priceRange * 0.05;
        }
    }

    /**
     * Main draw method
     */
    private void draw() {
        if (candles.isEmpty()) {
            return;
        }
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        // Clear canvas with theme background
        Color backgroundColor = Color.web(themeManager.getBgSecondary());
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, width, height);
        
        // Calculate chart dimensions
        double chartWidth = width - 2 * CHART_PADDING;
        double chartHeight = height - 2 * CHART_PADDING;
        double priceChartHeight = chartHeight * (1 - VOLUME_HEIGHT_RATIO);
        double volumeChartHeight = chartHeight * VOLUME_HEIGHT_RATIO;
        
        // Draw grid
        drawGrid(gc, width, height, priceChartHeight);
        
        // Draw candles
        drawCandles(gc, chartWidth, priceChartHeight);
        
        // Draw volume bars
        drawVolume(gc, chartWidth, priceChartHeight, volumeChartHeight);
        
        // Draw axes
        drawAxes(gc, width, height, priceChartHeight);
    }

    /**
     * Draw background grid
     */
    private void drawGrid(GraphicsContext gc, double width, double height, double priceChartHeight) {
        Color gridColor = Color.web(themeManager.getBorder()).deriveColor(0, 1, 1, 0.3);
        gc.setStroke(gridColor);
        gc.setLineWidth(1);
        
        // Horizontal grid lines (price levels)
        int numHorizontalLines = 5;
        for (int i = 0; i <= numHorizontalLines; i++) {
            double y = CHART_PADDING + (priceChartHeight / numHorizontalLines) * i;
            gc.strokeLine(CHART_PADDING, y, width - CHART_PADDING, y);
        }
        
        // Vertical grid lines (time)
        int numVerticalLines = Math.min(10, candles.size());
        if (numVerticalLines > 0) {
            double candleSpacing = (width - 2 * CHART_PADDING) / candles.size();
            for (int i = 0; i <= numVerticalLines; i++) {
                double x = CHART_PADDING + (width - 2 * CHART_PADDING) / numVerticalLines * i;
                gc.strokeLine(x, CHART_PADDING, x, height - CHART_PADDING);
            }
        }
    }

    /**
     * Draw candlesticks
     */
    private void drawCandles(GraphicsContext gc, double chartWidth, double priceChartHeight) {
        if (candles.isEmpty()) return;
        
        double candleSpacing = chartWidth / candles.size();
        double candleWidth = candleSpacing * CANDLE_WIDTH_RATIO;
        double priceRange = maxPrice - minPrice;
        
        // Prevent division by zero
        if (priceRange <= 0) priceRange = 1.0;
        
        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            double x = CHART_PADDING + candleSpacing * i + candleSpacing / 2;
            
            // Clamp drawing values to chart bounds to prevent drawing way outside canvas
            double openVal = Math.max(minPrice, Math.min(maxPrice, candle.open()));
            double closeVal = Math.max(minPrice, Math.min(maxPrice, candle.close()));
            double highVal = Math.max(minPrice, Math.min(maxPrice, candle.high()));
            double lowVal = Math.max(minPrice, Math.min(maxPrice, candle.low()));
            
            // Calculate Y positions (inverted because canvas Y increases downward)
            double openY = CHART_PADDING + priceChartHeight * (1 - (openVal - minPrice) / priceRange);
            double closeY = CHART_PADDING + priceChartHeight * (1 - (closeVal - minPrice) / priceRange);
            double highY = CHART_PADDING + priceChartHeight * (1 - (highVal - minPrice) / priceRange);
            double lowY = CHART_PADDING + priceChartHeight * (1 - (lowVal - minPrice) / priceRange);
            
            Color candleColor = candle.isBullish() ? BULLISH_COLOR : BEARISH_COLOR;
            
            // Draw wick (high-low line)
            Color wickColor = Color.web(themeManager.getTextSecondary()).deriveColor(0, 1, 1, 0.5);
            gc.setStroke(wickColor);
            gc.setLineWidth(1);
            gc.strokeLine(x, highY, x, lowY);
            
            // Draw body (open-close rectangle)
            gc.setFill(candleColor);
            double bodyTop = Math.min(openY, closeY);
            double bodyHeight = Math.abs(closeY - openY);
            
            // Ensure minimum body height for doji candles
            if (bodyHeight < 1) {
                bodyHeight = 1;
            }
            
            gc.fillRect(x - candleWidth / 2, bodyTop, candleWidth, bodyHeight);
        }
    }

    /**
     * Draw volume bars at bottom
     */
    private void drawVolume(GraphicsContext gc, double chartWidth, double priceChartHeight, double volumeChartHeight) {
        if (candles.isEmpty() || maxVolume == 0) return;
        
        double candleSpacing = chartWidth / candles.size();
        double barWidth = candleSpacing * CANDLE_WIDTH_RATIO;
        double volumeTop = CHART_PADDING + priceChartHeight;
        
        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            double x = CHART_PADDING + candleSpacing * i + candleSpacing / 2;
            double volumeHeight = (candle.volume() / maxVolume) * volumeChartHeight;
            
            Color volumeColor = candle.isBullish() ? 
                BULLISH_COLOR.deriveColor(0, 1, 1, 0.3) : 
                BEARISH_COLOR.deriveColor(0, 1, 1, 0.3);
            
            gc.setFill(volumeColor);
            gc.fillRect(x - barWidth / 2, volumeTop + volumeChartHeight - volumeHeight, barWidth, volumeHeight);
        }
    }

    /**
     * Draw axes with labels
     */
    private void drawAxes(GraphicsContext gc, double width, double height, double priceChartHeight) {
        Color textColor = Color.web(themeManager.getTextSecondary());
        gc.setFill(textColor);
        gc.setFont(Font.font("Segoe UI", 10));
        
        // Price labels (Y-axis)
        int numPriceLabels = 5;
        for (int i = 0; i <= numPriceLabels; i++) {
            double price = minPrice + (maxPrice - minPrice) / numPriceLabels * (numPriceLabels - i);
            double y = CHART_PADDING + (priceChartHeight / numPriceLabels) * i;
            
            String priceText = String.format("$%.2f", price);
            gc.fillText(priceText, width - CHART_PADDING + 5, y + 4);
        }
        
        // Time labels (X-axis) - simplified for now
        if (!candles.isEmpty()) {
            gc.fillText("Time →", width / 2, height - 20);
        }
        
        // Chart title
        Color titleColor = Color.web(themeManager.getTextPrimary());
        gc.setFill(titleColor);
        gc.setFont(Font.font("Segoe UI", 12));
        gc.fillText("BTC/USDT", 10, 20);
    }

    /**
     * Clear all candles
     */
    public void clear() {
        candles.clear();
        draw();
    }
}
