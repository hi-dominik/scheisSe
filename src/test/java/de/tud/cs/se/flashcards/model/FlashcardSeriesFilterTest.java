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
import static org.junit.Assert.assertSame;

import java.util.Stack;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.Test;


public class FlashcardSeriesFilterTest {

    static class TestListDataListener implements ListDataListener {

        Stack<ListDataEvent> lastEvents = new Stack<ListDataEvent>();


        public void intervalRemoved(ListDataEvent e) {

            lastEvents.push(e);

        }


        public void intervalAdded(ListDataEvent e) {

            lastEvents.push(e);

        }


        public void contentsChanged(ListDataEvent e) {

            lastEvents.push(e);

        }


        public ListDataEvent lastEvent() {

            return lastEvents.peek();
        }

    };


    TestListDataListener ldl = new TestListDataListener();


    @Test public void testConstruction() {

        {
            FlashcardSeries dfs = new DefaultFlashcardSeries();
            FlashcardSeriesFilter fsf = new FlashcardSeriesFilter(dfs);
            assertSame(dfs, fsf.getSourceModel());
        }

        {
            FlashcardSeries dfs = new DefaultFlashcardSeries();
            Flashcard a_a = new Flashcard("a", "a");
            Flashcard aa_aa = new Flashcard("aa", "aa");
            dfs.createAddCardCommand(a_a).execute();
            dfs.createAddCardCommand(aa_aa).execute();
            FlashcardSeriesFilter fsf = new FlashcardSeriesFilter(dfs);
            assertEquals(2, fsf.getSize());
            assertSame(a_a, fsf.getElementAt(1));
            assertSame(aa_aa, fsf.getElementAt(0));
        }
    }


    @Test public void testCardManagement() {

        FlashcardSeries dfs = new DefaultFlashcardSeries();
        FlashcardSeriesFilter fsf = new FlashcardSeriesFilter(dfs);

        Flashcard a_a = new Flashcard("a", "a");
        Flashcard aa_aa = new Flashcard("aa", "aa");
        Flashcard abc_abc = new Flashcard("abc", "abc");
        Flashcard a_e = new Flashcard("a", "e");
        Flashcard d_d = new Flashcard("d", "d");

        fsf.createAddCardCommand(a_a).execute();
        assertEquals(1, fsf.getSize());
        assertSame(a_a, fsf.getElementAt(0));

        fsf.createAddCardCommand(aa_aa).execute();
        assertEquals(2, fsf.getSize());
        assertSame(aa_aa, fsf.getElementAt(0));
        assertSame(a_a, fsf.getElementAt(1));

        fsf.createRemoveCardsCommand(new int[] {
            0
        }).execute();
        assertEquals(1, fsf.getSize());
        assertSame(a_a, fsf.getElementAt(0));

        fsf.createAddCardCommand(abc_abc).execute();
        assertEquals(2, fsf.getSize());
        assertSame(abc_abc, fsf.getElementAt(0));

        fsf.createAddCardCommand(a_e).execute();
        assertEquals(3, fsf.getSize());
        assertSame(a_e, fsf.getElementAt(0));

        fsf.createAddCardCommand(d_d).execute();
        assertEquals(4, fsf.getSize());
        assertSame(d_d, fsf.getElementAt(0));

        fsf.createRemoveCardsCommand(new int[] {
            1
        }).execute();
        assertEquals(3, fsf.getSize());
        assertSame(a_a, fsf.getElementAt(2));
        assertSame(abc_abc, fsf.getElementAt(1));
        assertSame(d_d, fsf.getElementAt(0));

        fsf.createRemoveCardsCommand(new int[] {
                0, 1, 2
        }).execute();
        assertEquals(0, fsf.getSize());

        {
            fsf.createAddCardCommand(abc_abc.clone()).execute();
            fsf.createAddCardCommand(a_e.clone()).execute();
            Flashcard temp = new Flashcard("e", "e");
            fsf.createAddCardCommand(temp).execute();
            fsf.createAddCardCommand(d_d.clone()).execute();
            fsf.setSearchTerm("a");
            assertEquals(2, fsf.getSize());
            fsf.createAddCardCommand(aa_aa.clone()).execute();
            assertEquals(3, fsf.getSize());
            temp.createUpdateCommand(temp.getQuestion(), "a").execute();
            assertEquals(4, fsf.getSize());
        }
    }


    @Test public void testFilteringAndEventHandling() {

        FlashcardSeries dfs = new DefaultFlashcardSeries();
        FlashcardSeriesFilter fsf = new FlashcardSeriesFilter(dfs);

        fsf.addListDataListener(ldl);

        Flashcard a_a = new Flashcard("a", "a");
        Flashcard aa_aa = new Flashcard("aa", "aa");
        Flashcard abc_abc = new Flashcard("abc", "abc");
        Flashcard a_e = new Flashcard("a", "e");
        Flashcard d_d = new Flashcard("d", "d");

        // test with one element
        fsf.createAddCardCommand(a_a).execute();
        {
            assertSame(fsf, ldl.lastEvent().getSource());
            assertEquals(0, ldl.lastEvent().getIndex0());
            assertEquals(0, ldl.lastEvent().getIndex1());
            assertEquals(1, fsf.getSize());
            assertSame(a_a, fsf.getElementAt(0));
        }
        {
            Object lastEvent = ldl.lastEvent();
            fsf.setSearchTerm("a");
            // check that all elements are still in the list... and that no event is generated
            assertEquals(1, fsf.getSize());
            assertSame(a_a, fsf.getElementAt(0));
            assertSame(lastEvent, ldl.lastEvent());
        }
        {
            fsf.setSearchTerm("b");
            // check that the list is now empty
            assertEquals(0, fsf.getSize());
            assertEquals(ListDataEvent.INTERVAL_REMOVED, ldl.lastEvent().getType());
            assertEquals(0, ldl.lastEvent().getIndex0());
            assertEquals(0, ldl.lastEvent().getIndex1());
        }
        {
            fsf.setSearchTerm("");
            // check that the list contains all elements
            assertEquals(1, fsf.getSize());
            assertSame(a_a, fsf.getElementAt(0));
            assertSame(ListDataEvent.INTERVAL_ADDED, ldl.lastEvent().getType());
            assertEquals(0, ldl.lastEvent().getIndex0());
            assertEquals(0, ldl.lastEvent().getIndex1());
        }

        // adding a second card; "no filter" set
        fsf.createAddCardCommand(aa_aa).execute();
        assertSame(ListDataEvent.INTERVAL_ADDED, ldl.lastEvent().getType());
        assertEquals(0, ldl.lastEvent().getIndex0());
        assertEquals(0, ldl.lastEvent().getIndex1());
        {
            fsf.setSearchTerm("a");
            // check that the list contains all elements
            assertEquals(2, fsf.getSize());
            assertSame(aa_aa, fsf.getElementAt(0));
            assertSame(a_a, fsf.getElementAt(1));
        }
        {
            fsf.setSearchTerm("aa");
            assertEquals(1, fsf.getSize());
            assertSame(ListDataEvent.INTERVAL_REMOVED, ldl.lastEvent().getType());
            assertEquals(1, ldl.lastEvent().getIndex0());
            assertEquals(1, ldl.lastEvent().getIndex1());
            assertSame(aa_aa, fsf.getElementAt(0));

        }
        {
            fsf.setSearchTerm("ab");
            assertEquals(0, fsf.getSize());
        }

        {
            fsf.setSearchTerm("");
            assertEquals(2, fsf.getSize());
        }

        fsf.createAddCardCommand(abc_abc).execute();
        fsf.createAddCardCommand(a_e).execute();
        assertEquals(4, fsf.getSize());

        // let's try what happens if we add / remove some elements while a filter is applied.
        fsf.setSearchTerm("a");
        assertEquals(4, fsf.getSize());

        fsf.createAddCardCommand(d_d).execute();
        assertEquals(4, fsf.getSize());
        fsf.setSearchTerm("d");
        assertEquals(1, fsf.getSize());
        assertSame(d_d, fsf.getElementAt(0));

        fsf.setSearchTerm("");
        assertEquals(5, fsf.getSize());

        fsf.setSearchTerm("a");
        assertEquals(4, fsf.getSize());

        fsf.setSearchTerm("aA");
        assertEquals(0, fsf.getSize());

        fsf.setSearchTerm("a");
        assertEquals(4, fsf.getSize());

        fsf.setSearchTerm("ab");
        assertEquals(1, fsf.getSize());

        fsf.setSearchTerm("a");
        assertEquals(4, fsf.getSize());

        int events = ldl.lastEvents.size();
        fsf.setSearchTerm("a"); // same search as before...
        assertEquals(events, ldl.lastEvents.size());

        a_a.createUpdateCommand(a_a.getQuestion(), "Z");
        assertEquals(4, fsf.getSize());

    }
}
