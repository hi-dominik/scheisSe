/** License (BSD Style License):
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
package de.tud.cs.se.flashcards.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Tests the CommandHistory
 * 
 * @author Michael Eichberg
 */
public class CommandHistoryTest {
    @Test public void testHistoryManagement() {

        CommandHistory commandHistory = new CommandHistory();

        FlashcardSeries flashcards = new DefaultFlashcardSeries();

        commandHistory.execute(flashcards.createAddCardCommand(new Flashcard("1", "1")));
        assertEquals(1, commandHistory.undoableCommandsCount());
        assertEquals(0, commandHistory.redoableCommandsCount());
        commandHistory.execute(flashcards.createAddCardCommand(new Flashcard("2", "2")));
        assertEquals(2, commandHistory.undoableCommandsCount());
        assertEquals(0, commandHistory.redoableCommandsCount());
        commandHistory.execute(flashcards.createAddCardCommand(new Flashcard("3", "3")));
        assertEquals(3, commandHistory.undoableCommandsCount());
        assertEquals(0, commandHistory.redoableCommandsCount());
        commandHistory.execute(flashcards.createAddCardCommand(new Flashcard("4", "4")));
        assertEquals(4, commandHistory.undoableCommandsCount());
        assertEquals(0, commandHistory.redoableCommandsCount());

        commandHistory.undo();
        assertEquals(3, commandHistory.undoableCommandsCount());
        assertEquals(1, commandHistory.redoableCommandsCount());
        commandHistory.undo();
        assertEquals(2, commandHistory.undoableCommandsCount());
        assertEquals(2, commandHistory.redoableCommandsCount());

        commandHistory.execute(flashcards.createAddCardCommand(new Flashcard("5", "5")));
        assertEquals(3, commandHistory.undoableCommandsCount());
        assertEquals(0, commandHistory.redoableCommandsCount());
        commandHistory.undo();
        assertEquals(2, commandHistory.undoableCommandsCount());
        assertEquals(1, commandHistory.redoableCommandsCount());
        commandHistory.redo();
        assertEquals(3, commandHistory.undoableCommandsCount());
        assertEquals(0, commandHistory.redoableCommandsCount());

    }
}
