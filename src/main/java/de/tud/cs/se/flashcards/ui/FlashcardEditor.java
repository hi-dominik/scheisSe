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

import static javax.swing.BorderFactory.createBevelBorder;
import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import de.tud.cs.se.flashcards.model.Command;
import de.tud.cs.se.flashcards.model.Flashcard;
import de.tud.cs.st.constraints.NotNull;


/**
 * Editor for flashcards.
 *
 * @author Michael Eichberg
 */
class FlashcardEditor {

    // GUI components

    private final JDialog dialog;

    private final JTextField questionField;

    private final JTextArea answerTextArea;

    /**
     * Creates a new editor that can be used to edit specific flashcards.
     *
     * @param owner
     *           This editor's parent frame.
     */
    FlashcardEditor(@NotNull FlashcardsEditor owner) {

        questionField = new JTextField();
        questionField.setAlignmentX(0.0f);

        JLabel questionLabel = new JLabel("Question");
        questionLabel.setBorder(createEmptyBorder(2, 2, 2, 2));
        questionLabel.setAlignmentX(0.0f);

        Box questionBox = Box.createVerticalBox();
        questionBox.setBorder(createCompoundBorder(
                createEmptyBorder(10, 10, 10, 10),
                createBevelBorder(BevelBorder.LOWERED)));
        questionBox.add(questionLabel);
        questionBox.add(Box.createVerticalStrut(5));
        questionBox.add(questionField);

        JLabel answerLabel = new JLabel("Answer");
        answerLabel.setAlignmentX(0.0f);
        answerLabel.setBorder(createEmptyBorder(2, 2, 2, 2));

        answerTextArea = new JTextArea();
        answerTextArea.setLineWrap(true);
        answerTextArea.setWrapStyleWord(true);

        JScrollPane answerTextAreaScrollPane = new JScrollPane(answerTextArea);
        answerTextAreaScrollPane.setAlignmentX(0.0f);
        answerTextAreaScrollPane.setBorder(UIManager.getBorder("ScrollPane.border"));
        answerTextAreaScrollPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Box answerBox = Box.createVerticalBox();
        answerBox.setBorder(createCompoundBorder(
                createEmptyBorder(10, 10, 10, 10),
                createBevelBorder(BevelBorder.LOWERED)));
        answerBox.add(answerLabel);
        answerBox.add(Box.createVerticalStrut(5));
        answerBox.add(answerTextAreaScrollPane);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                update = true;
                dialog.setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                update = false;
                dialog.setVisible(false);
            }
        });

        Box okCancelBox = Box.createHorizontalBox();
        okCancelBox.setBorder(createEmptyBorder(10, 10, 10, 10));
        okCancelBox.add(Box.createGlue());
        okCancelBox.add(cancelButton);
        okCancelBox.add(okButton);

        dialog = new JDialog(owner.getFrame(), "Edit Flashcard", true);
        dialog.getRootPane().putClientProperty("Window.style", "small");
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setMinimumSize(new java.awt.Dimension(320, 240));
        dialog.setSize(640, 480);
        dialog.setLocationRelativeTo(owner.getFrame());
        dialog.getContentPane().add(questionBox, BorderLayout.NORTH);
        dialog.getContentPane().add(answerBox);
        dialog.getContentPane().add(okCancelBox, BorderLayout.SOUTH);
    }


    /**
     * True if the card needs to be updated, false otherwise. This variable is set when the dialog is
     * closed.
     */
    private boolean update = false;


    /**
     * Edits a given flashcard. If the flashcard was edited and the editing process was not canceled,
     * <code>true</code> is returned.
     *
     * @param card
     *           The flashcard that may be edited. The flashcard is used to initialize this dialog.
     * @return <code>true</code> if the card was edited; <code>false</code> otherwise.
     */
    public Command edit(@NotNull Flashcard card) {

        // set to false to make sure that the card is not updated, when the dialog
        // is closed using the dialogs "close" button
        update = false;

        // configure the editor
        questionField.setText(card.getQuestion());
        answerTextArea.setText(card.getAnswer());

        // show the dialog to enable the user to edit the flashcard
        dialog.setVisible(true);

        // the dialog is closed
        if (update) {
            if (questionField.getText().length() == 0) {
                return card.createUpdateCommand(" ", answerTextArea.getText());
            } else {
                return card.createUpdateCommand(questionField.getText(), answerTextArea.getText());
            }
        } else
            return null;
    }

}
