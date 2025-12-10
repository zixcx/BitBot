package com.bitbot.client.ui.questionnaire;

import com.bitbot.client.dto.QuestionnaireQuestionsDto;
import com.bitbot.client.dto.QuestionnaireResultDto;
import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.components.ToastNotification;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 투자 성향 설문조사 화면
 */
public class QuestionnaireView extends StackPane {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireView.class);
    
    private final ThemeManager themeManager;
    private final ServerApiClient serverApiClient;
    private final ToastNotification toast;
    
    private final Map<String, ToggleGroup> answerGroups = new HashMap<>();
    private Button submitButton;
    private QuestionnaireListener listener;
    private VBox questionsContainer;
    private QuestionnaireQuestionsDto questionsData;
    private ProgressIndicator loadingIndicator;
    private VBox mainContent;

    public QuestionnaireView(ServerApiClient serverApiClient) {
        this.themeManager = ThemeManager.getInstance();
        this.serverApiClient = serverApiClient;
        this.toast = new ToastNotification(this);
        initializeUI();
        setupThemeBinding();
        loadQuestions();
    }

    private void initializeUI() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainContent = new VBox(30);
        mainContent.setPadding(new Insets(30));
        mainContent.setMaxWidth(900);
        mainContent.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));

        // Header
        VBox header = createHeader();
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        VBox loadingBox = new VBox(loadingIndicator, new Label("설문조사를 불러오는 중..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(15);
        loadingBox.setPadding(new Insets(50));
        
        // Questions container (will be populated after loading)
        questionsContainer = new VBox(20);

        mainContent.getChildren().addAll(header, loadingBox);
        
        VBox wrapper = new VBox(mainContent);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));
        
        scrollPane.setContent(wrapper);
        getChildren().add(scrollPane);
        
        applyTheme();
    }

    private void loadQuestions() {
        serverApiClient.getQuestionnaireQuestions()
            .thenAccept(questions -> {
                Platform.runLater(() -> {
                    this.questionsData = questions;
                    displayQuestions();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    toast.showError("설문조사 로딩 실패: " + ex.getMessage());
                    logger.error("Failed to load questions", ex);
                });
                return null;
            });
    }

    private void displayQuestions() {
        // Remove loading indicator
        mainContent.getChildren().removeIf(node -> node instanceof VBox && 
            ((VBox)node).getChildren().stream().anyMatch(child -> child instanceof ProgressIndicator));
        
        questionsContainer.getChildren().clear();
        answerGroups.clear();
        
        if (questionsData == null || questionsData.getSections() == null) {
            return;
        }
        
        // Add all sections
        for (QuestionnaireQuestionsDto.QuestionSection section : questionsData.getSections()) {
            VBox sectionBox = createSection(section);
            questionsContainer.getChildren().add(sectionBox);
        }
        
        // Submit button
        HBox submitBox = createSubmitButton();
        
        mainContent.getChildren().addAll(questionsContainer, submitBox);
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));

        FontIcon icon = new FontIcon(FontAwesomeSolid.CLIPBOARD_LIST);
        icon.setIconSize(48);
        icon.setIconColor(javafx.scene.paint.Color.web(ThemeManager.COLOR_PRIMARY));

        Label title = new Label("투자 성향 설문조사");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        Label subtitle = new Label("귀하의 투자 성향을 파악하여 최적의 거래 전략을 설정합니다");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        header.getChildren().addAll(icon, title, subtitle);
        return header;
    }

    private VBox createSection(QuestionnaireQuestionsDto.QuestionSection section) {
        VBox sectionBox = new VBox(15);
        sectionBox.setPadding(new Insets(20));
        sectionBox.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-border-radius: 10;
            """, themeManager.getBgSecondary(), themeManager.getBorder()));

        // Section header
        Label sectionTitle = new Label(section.getTitle());
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        Label sectionDesc = new Label(section.getDescription());
        sectionDesc.setFont(Font.font("Segoe UI", 13));
        sectionDesc.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
        sectionDesc.setWrapText(true);

        sectionBox.getChildren().addAll(sectionTitle, sectionDesc);

        // Questions in this section
        if (section.getQuestions() != null) {
            for (QuestionnaireQuestionsDto.Question question : section.getQuestions()) {
                VBox questionBox = createQuestion(question);
                sectionBox.getChildren().add(questionBox);
            }
        }

        return sectionBox;
    }

    private VBox createQuestion(QuestionnaireQuestionsDto.Question question) {
        VBox questionBox = new VBox(12);
        questionBox.setPadding(new Insets(15, 0, 0, 0));

        // Question text
        Label questionText = new Label(question.getText());
        questionText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        questionText.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));
        questionText.setWrapText(true);

        questionBox.getChildren().add(questionText);

        // Options
        ToggleGroup group = new ToggleGroup();
        answerGroups.put(question.getId().toLowerCase(), group);

        if (question.getOptions() != null) {
            for (QuestionnaireQuestionsDto.QuestionOption option : question.getOptions()) {
                RadioButton radio = new RadioButton(option.getText());
                radio.setToggleGroup(group);
                radio.setUserData(option.getValue());
                radio.setFont(Font.font("Segoe UI", 14));
                radio.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));
                questionBox.getChildren().add(radio);
            }
        }

        return questionBox;
    }

    private HBox createSubmitButton() {
        HBox submitBox = new HBox();
        submitBox.setAlignment(Pos.CENTER);
        submitBox.setPadding(new Insets(30, 0, 30, 0));

        submitButton = new Button("설문 제출하기");
        submitButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        submitButton.setPrefSize(200, 50);
        submitButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """, ThemeManager.COLOR_PRIMARY));

        submitButton.setOnAction(e -> handleSubmit());

        submitBox.getChildren().add(submitButton);
        return submitBox;
    }

    private void handleSubmit() {
        // Validate all questions answered
        int expectedQuestions = questionsData != null ? questionsData.getTotalQuestions() : 0;
        if (answerGroups.size() < expectedQuestions) {
            toast.showWarning("모든 질문에 답변해주세요");
            return;
        }

        for (Map.Entry<String, ToggleGroup> entry : answerGroups.entrySet()) {
            if (entry.getValue().getSelectedToggle() == null) {
                toast.showWarning("모든 질문에 답변해주세요");
                return;
            }
        }

        // Collect answers
        Map<String, Integer> answers = new HashMap<>();
        for (Map.Entry<String, ToggleGroup> entry : answerGroups.entrySet()) {
            Toggle selected = entry.getValue().getSelectedToggle();
            if (selected != null) {
                answers.put(entry.getKey(), (Integer) selected.getUserData());
            }
        }

        // Disable submit button
        submitButton.setDisable(true);
        submitButton.setText("제출 중...");

        // Submit
        serverApiClient.submitQuestionnaire(answers)
            .thenAccept(result -> {
                Platform.runLater(() -> {
                    toast.showSuccess("설문조사가 완료되었습니다!");
                    logger.info("Questionnaire completed: " + result.getInvestorType());
                    
                    if (listener != null) {
                        listener.onQuestionnaireCompleted(result);
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    toast.showError("설문 제출 실패: " + ex.getMessage());
                    logger.error("Failed to submit questionnaire", ex);
                    submitButton.setDisable(false);
                    submitButton.setText("설문 제출하기");
                });
                return null;
            });
    }

    private void applyTheme() {
        setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
            if (mainContent != null) {
                mainContent.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));
            }
        });
    }

    public void setListener(QuestionnaireListener listener) {
        this.listener = listener;
    }

    public interface QuestionnaireListener {
        void onQuestionnaireCompleted(QuestionnaireResultDto result);
    }
}
