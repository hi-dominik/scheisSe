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
package de.tud.cs.se.flashcards.model.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.tud.cs.se.flashcards.model.Flashcard;
import de.tud.cs.se.flashcards.model.DefaultFlashcardSeries;


public class RandomLearningStrategyTest {

    @Test
    public void testLearningStrategy() {

        assertNotNull(RandomLearningStrategy.INFO.getShortDescription());
        assertTrue(RandomLearningStrategy.INFO.getShortDescription().length() > 0);

        DefaultFlashcardSeries fs = DefaultFlashcardSeries.createInitialFlashcardSeries();
        assertTrue(fs.getSize() > 4); // basically just a prerequisite for the following tests

        LearningStrategy ls = RandomLearningStrategy.INFO.create(fs);
        Set<Flashcard> s = new HashSet<Flashcard>();

        int c = 0;
        while (ls.hasNext()) {
            c++;
            ls.next();
            Flashcard next = ls.current();
            assertFalse(s.contains(next));
            s.add(next);
        }
        assertEquals(fs.getSize(), c);

        try {
            ls.current();
        } catch (IndexOutOfBoundsException e) {
            fail("\"hasNext\" should not advance the iterator!.");
        }
        try {
            ls.next();
            fail("Iteration beyond the end of the series is possible.");
        } catch (IndexOutOfBoundsException e) {
            // ok - as intended
        }
        try {
            ls.current();
            fail("More elements are returned than the list contains.");
        } catch (IndexOutOfBoundsException e) {
            // Intended
        }

    }
}
