/*  License (BSD Style License):
 *  Copyright (c) 2010
 *  Software Engineering
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Engineering Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package de.tud.cs.se.flashcards.ui;

import static de.tud.cs.se.flashcards.ui.Utilities.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import de.tud.cs.se.flashcards.model.Flashcard;
import de.tud.cs.se.flashcards.model.learning.LearningStrategy;
import de.tud.cs.st.constraints.NotNull;


/**
 * This dialog first renders a flashcard's question and then the answer. Additionally, the logic to
 * step through a series of flashcards is provided.
 *
 * @author Michael Eichberg
 */
public class LearnDialog {

    private final FlashcardsEditor owner;


    private final JDialog dialog;

    private final JLabel titleLabel;


    private final JLabel elapsedTimeLabel;

    private final JLabel contentLabel;


    private final Box navigationBox;


    private final JButton rememberedButton;


    private final JButton forgottenButton;


    private final JButton flipButton;

    /**
     * State of the dialog.
     */
    private enum State {
        SHOWS_ANSWER,
        SHOWS_QUESTION
    }

    private State currentState = null;


    private LearningStrategy learningStrategy; // set by the show method


    LearnDialog(@NotNull FlashcardsEditor owner) {

        this.owner = owner;

        dialog = new JDialog(owner.getFrame(), true);
        dialog.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.TRUE);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        // Create the header components:
        titleLabel = new JLabel(); // need to be initialized...
        titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 10, 10));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        titleLabel.setPreferredSize(new JLabel("XXXXXXXXXX").getPreferredSize());

        elapsedTimeLabel = new JLabel();

        JButton editButton = new JButton(
                createImageIcon("accessories-text-editor-small.png", "stop learning"));
        editButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 2));
        editButton.addActionListener(e -> {

            if (LearnDialog.this.owner.editFlashcard(learningStrategy.current())) {
                updateContentLabel();
            }
        });

        JButton cancelButton = new JButton(createImageIcon("process-stop.png", "stop learning"));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 2));
        cancelButton.addActionListener(e -> {

            stopElapsedTimeUpdater();
            dialog.setVisible(false);
        });

        Box headerBox = Box.createHorizontalBox();
        headerBox.add(titleLabel);
        headerBox.add(Box.createHorizontalGlue());
        headerBox.add(elapsedTimeLabel);
        headerBox.add(Box.createHorizontalGlue());
        headerBox.add(editButton);
        headerBox.add(cancelButton);

        contentLabel = new JLabel(); // need to be initialized...
        contentLabel.setSize(Flashcard.FLASHCARD_DIMENSION);
        contentLabel.setPreferredSize(Flashcard.FLASHCARD_DIMENSION);
        contentLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        contentLabel.setFont(UIManager.getFont("FormattedTextField.font"));
        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // setup the footer
        rememberedButton = new JButton(createImageIcon("face-grin.png", "Remembered"));
        rememberedButton.addActionListener(event -> remembered());
        forgottenButton = new JButton(createImageIcon("face-sad.png", "Forgotten"));
        forgottenButton.addActionListener(event -> forgotten());
        flipButton = new JButton(createImageIcon("go-next.png", "Forgotten"));
        flipButton.addActionListener(event -> showAnswer());

        navigationBox = Box.createHorizontalBox();
        navigationBox.add(Box.createHorizontalGlue());

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(headerBox, BorderLayout.NORTH);
        dialog.getContentPane().add(contentLabel, BorderLayout.CENTER);
        dialog.getContentPane().add(navigationBox, BorderLayout.SOUTH);

        dialog.addWindowListener(new WindowAdapter() {

            @Override public void windowClosed(WindowEvent e) {

                stopElapsedTimeUpdater();
            }

        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // dialog.setLocationRelativeTo(owner.getFrame());
        dialog.setLocation(screenSize.width / 2 - (320), screenSize.height / 2 - 240);
        dialog.setUndecorated(true);
        ((JComponent) dialog.getContentPane()).setBorder(BorderFactory
                .createBevelBorder(BevelBorder.RAISED));
        dialog.pack();
    }


    // The "core" logic:

    /**
     * Opens the dialog and shows the flashcards in the order defined by the given learning strategy.
     *
     * @param learningStrategy
     *           The learning strategy that is used.
     */
    public void show(@NotNull LearningStrategy learningStrategy) {

        // check the stated precondition
        assert owner.getSortedFlashcards().getSize() > 0;

        this.learningStrategy = learningStrategy;

        // Initialization of the dialog:
        showNextQuestion();
        elapsedTimeLabel.setText("0:00");
        startElapsedTimeUpdater();

        // show dialog
        dialog.setVisible(true);
    }


    private void showAnswer() {

        currentState = State.SHOWS_ANSWER;

        updateContentLabel();
        titleLabel.setText("Answer");

        navigationBox.remove(flipButton);
        navigationBox.add(forgottenButton);
        navigationBox.add(rememberedButton);

        dialog.getContentPane().revalidate();
        dialog.repaint();
    }


    private void remembered() {

        learningStrategy.current().setRemembered(LocalDate.now());
        showNextQuestion();
    }


    private void forgotten() {

        learningStrategy.current().setNotRemembered(LocalDate.now());
        showNextQuestion();
    }


    private void showNextQuestion() {

        currentState = State.SHOWS_QUESTION;

        if (learningStrategy.hasNext()) {
            learningStrategy.next();

            updateContentLabel();
            titleLabel.setText("Question");

            navigationBox.remove(rememberedButton);
            navigationBox.remove(forgottenButton);
            navigationBox.add(flipButton);

            dialog.getContentPane().revalidate();
            dialog.repaint();
        } else {
            stopElapsedTimeUpdater();

            dialog.setVisible(false);
        }
    }


    private void updateContentLabel() {

        switch (currentState) {
        case SHOWS_QUESTION:
            contentLabel.setText(learningStrategy.current().getQuestion());
            break;
        case SHOWS_ANSWER:
            contentLabel.setText(learningStrategy.current().getAnswer());
            break;
        default:
            throw new Error();
        }
    }


    // Fancy GUI related stuff:

    private Timer timeUpdater = null;


    private synchronized void startElapsedTimeUpdater() {

        assert timeUpdater == null;

        timeUpdater = new Timer(1000, new ActionListener() {

            private final long startTime = System.currentTimeMillis();


            public void actionPerformed(ActionEvent e) {

                long elapsedTime = System.currentTimeMillis() - startTime;
                double minutes = elapsedTime / (double) 1000;
                String s = String.format(
                        "%d:%02d",
                        (int) Math.floor((minutes / 60)),
                        (int) Math.round((minutes % 60)));
                elapsedTimeLabel.setText(s);

            }
        });
        timeUpdater.start();
    }


    private synchronized void stopElapsedTimeUpdater() {

        // depending on how the window is closed, this method may be called twice
        if (timeUpdater != null) {
            timeUpdater.stop();
            timeUpdater = null;
        }
    }

}
