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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.Test;


/**
 * Tests all (public) client methods of FlashcardSeries.
 * 
 * @author Michael Eichberg
 */
public class DefaultFlashcardSeriesTest {

    @Test public void testCreateInitialFlashcardSeries() {

        DefaultFlashcardSeries fs = DefaultFlashcardSeries.createInitialFlashcardSeries();
        assertTrue(fs.getSize() > 0);
    }


    @Test public void testAdd_RemoveListDataListener() {

        class TestListDataListener implements ListDataListener {

            public void contentsChanged(ListDataEvent e) {

            }


            public void intervalAdded(ListDataEvent e) {

            }


            public void intervalRemoved(ListDataEvent e) {

            }

        }

        ListDataListener l1 = new TestListDataListener();
        ListDataListener l2 = new TestListDataListener();

        DefaultFlashcardSeries fs = new DefaultFlashcardSeries();
        assertEquals(0, fs.getListDataListeners().length);

        fs.addListDataListener(l1);
        fs.addListDataListener(l2);
        assertEquals(2, fs.getListDataListeners().length);
        assertSame(l1, fs.getListDataListeners()[0]);
        assertSame(l2, fs.getListDataListeners()[1]);

        fs.removeListDataListener(l1);
        assertEquals(1, fs.getListDataListeners().length);
        assertSame(l2, fs.getListDataListeners()[0]);

        fs.removeListDataListener(l2);
        assertEquals(0, fs.getListDataListeners().length);

        fs.addListDataListener(l1);
        fs.addListDataListener(l2);
        fs.removeListDataListener(l2);
        assertSame(l1, fs.getListDataListeners()[0]);
    }


    @Test public void testNotifications() {

        class RecordingListDataListener implements ListDataListener {

            private ListDataEvent lastEvent = null;


            public void contentsChanged(ListDataEvent e) {

                lastEvent = e;
            }


            public void intervalAdded(ListDataEvent e) {

                lastEvent = e;
            }


            public void intervalRemoved(ListDataEvent e) {

                lastEvent = e;
            }
        }

        RecordingListDataListener l1 = new RecordingListDataListener();
        RecordingListDataListener l2 = new RecordingListDataListener();
        FlashcardSeries fs = new DefaultFlashcardSeries();
        CommandHistory commandHistory = new CommandHistory();
        fs.addListDataListener(l1);
        fs.addListDataListener(l2);

        Flashcard a = new Flashcard("a", "a");
        Flashcard b = new Flashcard("b", "b");
        Flashcard c = new Flashcard("c", "c");

        fs.createAddCardCommand(a).execute();
        assertSame(l1.lastEvent, l2.lastEvent);
        assertEquals(ListDataEvent.INTERVAL_ADDED, l1.lastEvent.getType());
        assertEquals(0, l1.lastEvent.getIndex0());
        assertEquals(0, l1.lastEvent.getIndex1());
        assertEquals(ListDataEvent.INTERVAL_ADDED, l2.lastEvent.getType());
        assertEquals(0, l2.lastEvent.getIndex0());
        assertEquals(0, l2.lastEvent.getIndex1());

        fs.createAddCardCommand(b).execute();
        assertSame(l1.lastEvent, l2.lastEvent);
        assertEquals(ListDataEvent.INTERVAL_ADDED, l1.lastEvent.getType());
        assertEquals(0, l1.lastEvent.getIndex0());
        assertEquals(0, l1.lastEvent.getIndex1());

        fs.removeListDataListener(l2);

        fs.createAddCardCommand(c).execute();
        assertNotSame(l1.lastEvent, l2.lastEvent);
        assertEquals(ListDataEvent.INTERVAL_ADDED, l1.lastEvent.getType());
        assertEquals(0, l1.lastEvent.getIndex0());
        assertEquals(0, l1.lastEvent.getIndex1());

        commandHistory.execute(fs.createRemoveCardsCommand(new int[] {
            1
        }));
        assertEquals(ListDataEvent.INTERVAL_REMOVED, l1.lastEvent.getType());
        assertEquals(1, l1.lastEvent.getIndex0());
        assertEquals(1, l1.lastEvent.getIndex1());

        commandHistory.execute(a.createUpdateCommand(a.getQuestion(), "new"));
        assertEquals(ListDataEvent.CONTENTS_CHANGED, l1.lastEvent.getType());
        assertEquals(1, l1.lastEvent.getIndex0());
        assertEquals(1, l1.lastEvent.getIndex1());

        commandHistory.execute(fs.createRemoveCardsCommand(new int[] {
            0
        }));
        assertEquals(ListDataEvent.INTERVAL_REMOVED, l1.lastEvent.getType());
        assertEquals(0, l1.lastEvent.getIndex0());
        assertEquals(0, l1.lastEvent.getIndex1());
    }


    @Test public void testAddCard_RemoveCard_GetElementAt_GetSize() {

        FlashcardSeries fs = new DefaultFlashcardSeries();

        Flashcard a = new Flashcard("a", "a");
        Flashcard b = new Flashcard("b", "b");
        Flashcard c = new Flashcard("c", "c");

        assertEquals(0, fs.getSize());
        fs.createAddCardCommand(a).execute();
        assertEquals(1, fs.getSize());
        fs.createAddCardCommand(b).execute();
        fs.createAddCardCommand(c).execute();
        assertEquals(3, fs.getSize());
        assertSame(c, fs.getElementAt(0));
        assertSame(b, fs.getElementAt(1));
        assertSame(a, fs.getElementAt(2));

        fs.createRemoveCardsCommand(new int[] {
                0, 1
        }).execute();
        assertEquals(1, fs.getSize());
        assertSame(a, fs.getElementAt(0));

        fs.createRemoveCardsCommand(new int[] {
            0
        }).execute();
        try {
            fs.getElementAt(0);
            fail("Deleted flashcards can still be retrieved.");
        } catch (IndexOutOfBoundsException t) { /* OK */ }

        // remove all cards at once
        fs.createAddCardCommand(a.clone()).execute();
        fs.createAddCardCommand(b.clone()).execute();
        fs.createAddCardCommand(c.clone()).execute();
        assertEquals(3, fs.getSize());
        fs.createRemoveCardsCommand(new int[] {
                0, 1, 2
        }).execute();
        assertEquals(0, fs.getSize());
    }


    @Test public void testUndoRedoOfAddCard() {

        FlashcardSeries fs = new DefaultFlashcardSeries();

        Flashcard a = new Flashcard("a", "a");
        Flashcard b = new Flashcard("b", "b");
        Flashcard c = new Flashcard("c", "c");

        Command aC = fs.createAddCardCommand(a);
        aC.execute();
        Command bC = fs.createAddCardCommand(b);
        bC.execute();
        Command cC = fs.createAddCardCommand(c);
        cC.execute();
        assertEquals(3, fs.getSize());

        cC.unexecute();
        assertEquals(2, fs.getSize());
        assertSame(a, fs.getElementAt(1));
        assertSame(b, fs.getElementAt(0));

        bC.unexecute();
        assertEquals(1, fs.getSize());
        assertSame(a, fs.getElementAt(0));

        bC.execute();
        cC.execute();
        assertEquals(3, fs.getSize());
        assertSame(a, fs.getElementAt(2));
        assertSame(b, fs.getElementAt(1));
        assertSame(c, fs.getElementAt(0));

    }


    @Test public void testUndoRedoOfRemoveCard() {

        FlashcardSeries fs = new DefaultFlashcardSeries();

        Flashcard a = new Flashcard("a", "a");
        Flashcard b = new Flashcard("b", "b");
        Flashcard c = new Flashcard("c", "c");

        Command aC = fs.createAddCardCommand(a);
        aC.execute();
        Command bC = fs.createAddCardCommand(b);
        bC.execute();
        Command cC = fs.createAddCardCommand(c);
        cC.execute();
        assertEquals(3, fs.getSize());

        Command rC = fs.createRemoveCardsCommand(new int[] {
            1
        });
        rC.execute();
        assertEquals(2, fs.getSize());

        rC.unexecute();
        assertEquals(3, fs.getSize());
        assertSame(a, fs.getElementAt(2));
        assertSame(b, fs.getElementAt(1));
        assertSame(c, fs.getElementAt(0));

        rC.execute();
        assertEquals(2, fs.getSize());
        assertSame(a, fs.getElementAt(1));
        assertSame(c, fs.getElementAt(0));
    }
}
