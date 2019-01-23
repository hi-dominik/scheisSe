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
package de.tud.cs.se.flashcards.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.tud.cs.st.constraints.NotNull;

/**
 * Implementation of a flashcard list that is sorted.
 *
 * This flashcard series acts as a decorator for another flashcard series.
 *
 * @version $Revision: 1.1 $ $Date: 2007/06/14 06:48:16 $
 * @author Michael Eichberg
 */
public final class SortedFlashcardSeries extends AbstractFlashcardSeries {

    private final Comparator<Integer> timesRememberedInARow = new Comparator<Integer>() {

        public int compare(Integer f1, Integer f2) {

            int v = flashcardSeries.getElementAt(f1).getRememberedInARowCount()
                    - flashcardSeries.getElementAt(f2).getRememberedInARowCount();
            if (v != 0) {
                return v;
            } else {
                return flashcardSeries.getElementAt(f1).getCreationID()
                        - flashcardSeries.getElementAt(f2).getCreationID();
            }
        }
    };

    public Comparator<Integer> timesRememberedInARowStrategy() {
        return timesRememberedInARow;
    }

    /**
     * The order is "most recent - last"; i.e. the last card is the card that was remembered most
     * recently. Cards that were remembered a long time ago (or never) are found at the very
     * beginning.
     */
    private final Comparator<Integer> lastTimeRemembered = new Comparator<Integer>() {

        public int compare(Integer f1, Integer f2) {

            LocalDate f1d = flashcardSeries.getElementAt(f1).getRemembered();
            LocalDate f2d = flashcardSeries.getElementAt(f2).getRemembered();
            if (f1d == null && f2d == null)
                return flashcardSeries.getElementAt(f1).getCreationID()
                        - flashcardSeries.getElementAt(f2).getCreationID();
            if (f1d == null)
                return -1;
            if (f2d == null)
                return Integer.MAX_VALUE;

            int v = f1d.compareTo(f2d);
            if (v != 0) {
                return v;
            } else {
                return flashcardSeries.getElementAt(f1).getCreationID()
                        - flashcardSeries.getElementAt(f2).getCreationID();
            }
        }
    };

    public Comparator<Integer> lastTimeRememberedStrategy() {
        return timesRememberedInARow;
    }


    private final Comparator<Integer> dateCreated = new Comparator<Integer>() {

        public int compare(Integer f1, Integer f2) {

            LocalDate f1d = flashcardSeries.getElementAt(f1).getCreated();
            LocalDate f2d = flashcardSeries.getElementAt(f2).getCreated();

            int v = f2d.compareTo(f1d);
            if (v != 0) {
                return v;
            } else {
                return flashcardSeries.getElementAt(f1).getCreationID()
                        - flashcardSeries.getElementAt(f2).getCreationID();
            }
        }
    };

    public Comparator<Integer> dateCreatedStrategy() {
        return dateCreated;
    }


    /**
     * The underlying flashcard series.
     */
    private final FlashcardSeries flashcardSeries;


    private final ArrayList<Integer> flashcardReferences = new ArrayList<>();


    private @NotNull Comparator<Integer> sortingStrategy;

    public SortedFlashcardSeries(@NotNull FlashcardSeries flashcardSeries) {

        this.sortingStrategy = dateCreated;

        this.flashcardSeries = flashcardSeries;

        for (int i = 0; i < flashcardSeries.getSize(); i++)
            flashcardReferences.add(i);
        flashcardReferences.sort(sortingStrategy);

        // u_ => underlying
        // remap existing indices
        // insert new references...
        // u_ => underlying
        // the position may have changed...
        // negative if not included...
        // u_ => underlying
        // find indexes of references that we need to remove and remap the other indices
        // let's updated this model
        ListDataListener listDataListener = new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {

                // u_ => underlying
                int uStartIndex = e.getIndex0();
                int uEndIndex = e.getIndex1();
                int count = uEndIndex - uStartIndex + 1;

                // remap existing indices
                for (int i = 0; i < flashcardReferences.size(); i++) {
                    if (flashcardReferences.get(i) >= uStartIndex)
                        flashcardReferences.set(i, flashcardReferences.get(i) + count);
                }

                // insert new references...
                for (int uIndex = uStartIndex; uIndex <= uEndIndex; uIndex++) {
                    int position = Collections.binarySearch(
                            flashcardReferences,
                            uIndex,
                            SortedFlashcardSeries.this.sortingStrategy
                    );
                    if (position < 0)
                        position = -position - 1;
                    flashcardReferences.add(position, uIndex);
                    fireIntervalAdded(SortedFlashcardSeries.this, position, position);
                }
            }

            public void contentsChanged(ListDataEvent e) {

                // u_ => underlying
                int uStartIndex = e.getIndex0();
                int uEndIndex = e.getIndex1();

                for (int uIndex = uStartIndex; uIndex <= uEndIndex; uIndex++) {

                    // the position may have changed...
                    for (int index = 0; index < flashcardReferences.size(); index++) {
                        if (flashcardReferences.get(index) >= uStartIndex
                                && flashcardReferences.get(index) <= uEndIndex) {

                            flashcardReferences.remove(index);
                            fireIntervalRemoved(SortedFlashcardSeries.this, index, index);

                            int position = Collections.binarySearch(
                                    flashcardReferences,
                                    uIndex,
                                    SortedFlashcardSeries.this.sortingStrategy
                            ); // negative if not included...
                            if (position < 0)
                                position = -position - 1;
                            flashcardReferences.add(position, uIndex);
                            fireIntervalAdded(SortedFlashcardSeries.this, position, position);
                        }
                    }
                }
            }

            public void intervalRemoved(ListDataEvent e) {

                // u_ => underlying
                int uStartIndex = e.getIndex0();
                int uEndIndex = e.getIndex1();
                int count = uEndIndex - uStartIndex + 1;

                // find indexes of references that we need to remove and remap the other indices
                int[] indices = new int[count];
                int j = 0;
                for (int i = 0; i < flashcardReferences.size(); i++) {
                    if (flashcardReferences.get(i) >= uStartIndex) {
                        if (flashcardReferences.get(i) <= uEndIndex)
                            indices[j++] = i;
                        else
                            flashcardReferences.set(i, flashcardReferences.get(i) - count);
                    }
                }
                // let's updated this model
                Arrays.sort(indices);
                for (int i = indices.length - 1; i >= 0; i--) {
                    flashcardReferences.remove(indices[i]);
                }
                if (indices[indices.length - 1] - indices[0] + 1 == indices.length)
                    fireIntervalRemoved(SortedFlashcardSeries.this, indices[0],
                            indices[indices.length - 1]
                    );
                else
                    fireContentsUpdated(SortedFlashcardSeries.this, 0, flashcardReferences.size());
            }
        };
        flashcardSeries.addListDataListener(listDataListener);

    }


    public int getNextCreationID() {

        return flashcardSeries.getNextCreationID();
    }


    public void setNextCreationID(int creationID) {

        flashcardSeries.setNextCreationID(creationID);
    }


    public void setSortingStrategy(Comparator<Integer> sortingStrategy) {

        this.sortingStrategy = sortingStrategy;
        flashcardReferences.sort(sortingStrategy);

        fireContentsUpdated(this, 0, flashcardReferences.size() - 1);
    }


    /**
     * @return The source model of the underlying flashcards list.
     */
    public FlashcardSeries getSourceModel() {

        return flashcardSeries.getSourceModel();
    }


    public Command createAddCardCommand(@NotNull Flashcard flashcard) {

        return flashcardSeries.createAddCardCommand(flashcard);

    }


    public Command createRemoveCardsCommand(int[] indices) {

        // remap indices
        int[] realIndices = new int[indices.length];
        for (int i = indices.length - 1; i >= 0; i--) {
            realIndices[i] = flashcardReferences.get(indices[i]);
        }

        Arrays.sort(realIndices);

        // remove cards on the underlying collection
        return flashcardSeries.createRemoveCardsCommand(realIndices);
    }


    public Flashcard getElementAt(int index) throws IndexOutOfBoundsException {

        return flashcardSeries.getElementAt(flashcardReferences.get(index));
    }


    public int getSize() {

        return flashcardReferences.size();
    }
}
