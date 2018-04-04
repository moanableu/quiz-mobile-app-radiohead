package com.example.android.qtt;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long COUNTDOWN_IN_MILLIS = 31000;

    private TextView scoreView, questionCountDown, countdown;
    private TextView question;
    private RadioGroup rbGroup;
    private RadioButton rb1, rb2, rb3;
    private String selectedAnswer = "";
    private CheckBox cb1, cb2, cb3;
    private EditText typeAnswer;
    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRb;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    //init array list
    private List<Question> questionList;
    private int questionCounter; // q shown
    private int getQuestionCounter; // ttl q in array
    private Question currentQuestion; // displayed q

    private int score;
    private boolean answered;

    private long backPressTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreView = findViewById(R.id.score_view);
        questionCountDown = findViewById(R.id.question_count_view);
        countdown = findViewById(R.id.countdown);
        question = findViewById(R.id.question);
        rbGroup = findViewById(R.id.radio_group);
        rb1 = findViewById(R.id.button_1);
        rb2 = findViewById(R.id.button_2);
        rb3 = findViewById(R.id.button_3);
        cb1 = findViewById(R.id.checkbox_1);
        cb2 = findViewById(R.id.checkbox_2);
        cb3 = findViewById(R.id.checkbox_3);
        typeAnswer = findViewById(R.id.song_text);
        buttonConfirmNext = findViewById(R.id.button_continue);

        textColorDefaultRb = rb1.getTextColors(); // get default color

        // init dbHelper
        QuizDbHelper dbHelper = new QuizDbHelper(this);
        questionList = dbHelper.getAllQuestions();
        getQuestionCounter = questionList.size();
        Collections.shuffle(questionList);

        // hide views
        hideViews();

        // get Questions
        showNextQuestion();

        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!answered) {
                    if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked()) {
                        checkAnswer(); // triggers showSolution which in turn triggers hideViews
                    }
                    // not quite sure how to see if this is unchecked or compare to available valid answers
                    else if (cb1.isChecked() || cb2.isChecked() || cb3.isChecked()) {
                        checkAnswer();
                    }
                    else if (!typeAnswer.getText().toString().isEmpty()) {
                        checkAnswer();
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, "Please select an option", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 160);
                        toast.show();

                    } // toast does not come up ever!!
                } else {
                    showNextQuestion();
                }
            }
        });

        // prevent keyboard from covering UI when quiz is launched -- find how to use it only when EditText is shown!!
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void hideViews() { // added
        rbGroup.setVisibility(View.INVISIBLE);
        cb1.setVisibility(View.INVISIBLE);
        cb2.setVisibility(View.INVISIBLE);
        cb3.setVisibility(View.INVISIBLE);
        typeAnswer.setVisibility(View.INVISIBLE);
    }

    // display RadioGroup
    private void showRadioGroup() {
        rbGroup.setVisibility(View.VISIBLE);
    }

    // display checkboxes
    private void showCheckboxes() { // added
        cb1.setVisibility(View.VISIBLE);
        cb2.setVisibility(View.VISIBLE);
        cb3.setVisibility(View.VISIBLE);
    }

    // display EditText
    private void showTypeAnswer() {
        typeAnswer.setVisibility(View.VISIBLE);
    }

    private void showNextQuestion() {
        rbGroup.clearCheck();

        cb1.setChecked(false);
        cb2.setChecked(false);
        cb3.setChecked(false);

        answered = false;

        typeAnswer.getText().clear();

        if (questionCounter < getQuestionCounter) {
            currentQuestion = questionList.get(questionCounter);

            question.setText(currentQuestion.getQuestion());

            // next line updated by Causaelity R.S.

            switch (currentQuestion.getType()) {
                case RADIO:
                    showRadioGroup();
                    // radio options
                    rb1.setText(currentQuestion.getOption1());
                    rb2.setText(currentQuestion.getOption2());
                    rb3.setText(currentQuestion.getOption3());
                    break;
                case CHECKBOX:
                    showCheckboxes();

                    // checkbox options
                    cb1.setText(currentQuestion.getOption1());
                    cb2.setText(currentQuestion.getOption2());
                    cb3.setText(currentQuestion.getOption3());

                    break;
                case TEXTENTRY:
                    showTypeAnswer();
                    break;
            }

            questionCounter++;
            questionCountDown.setText("Question: " + questionCounter + "/" + getQuestionCounter);

            buttonConfirmNext.setText("Confirm");

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        countdown.setText(timeFormatted);
    }

    // calculate score
    private void checkAnswer() {
        answered = true;
        countDownTimer.cancel();
        boolean answeredCorrectly = false;

        int answerNumber = 0;

        switch (currentQuestion.getType()) {
            case RADIO:
                RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
                answerNumber = rbGroup.indexOfChild(rbSelected) + 1;
                if (answerNumber == currentQuestion.getAnswerNumber()) {
                    score++;
                    answeredCorrectly = true;

                }

                break;
            case CHECKBOX:

                if (cb1.isChecked()) { answerNumber += 1; }
                if (cb2.isChecked()) { answerNumber += 2; }
                if (cb3.isChecked()) { answerNumber += 4; }

                if (answerNumber == currentQuestion.getAnswerNumber()) {
                    score++;
                    answeredCorrectly = true;

                }

                break;
            case TEXTENTRY:
                if (typeAnswer.getText().toString().equalsIgnoreCase("in rainbows")) {
                    score++;
                    answeredCorrectly = true;

                }
                break;
        }

        scoreView.setText("Score: " + score);


        if (!answeredCorrectly) {
            showSolution();
        }

    }

    // compare answers to valid answer
    private void showSolution() {
        // call to enum updated by Causaelity R.S.
        if (currentQuestion.getType() == QuestionType.RADIO) {

            // if radiogroup then use this switch statement, else use the next one for cb1, cb2, cb3
            switch (currentQuestion.getAnswerNumber()) {
                case 1:
                    question.setText("Answer a) is correct");
                    break;
                case 2:
                    question.setText("Answer b) is correct");
                    break;
                case 3:
                    question.setText("Answer c) is correct");
                    break;
            }
        }
       else if (currentQuestion.getType() == QuestionType.CHECKBOX) {

            switch (currentQuestion.getAnswerNumber()) {
                case 1:
                    question.setText("Answer a) is correct");
                    break;
                case 2:
                    question.setText("Answer b) is correct");
                    break;
                case 3:
                    question.setText("Answer c) is correct");
                    break;
            }
        }
        else if (currentQuestion.getType() == QuestionType.TEXTENTRY) {
            typeAnswer.setText("In Rainbows");
        }
        hideViews();

        if (questionCounter < getQuestionCounter) {
            buttonConfirmNext.setText("Next");
        } else {
            buttonConfirmNext.setText("Done!");
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressTime + 2000 > System.currentTimeMillis()) {
            finishQuiz();
        } else {
            Toast.makeText(this, "Please press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void finishQuiz() {
        finish();
    }
}
