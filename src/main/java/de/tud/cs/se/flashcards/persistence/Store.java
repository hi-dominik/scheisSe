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
package de.tud.cs.se.flashcards.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.tud.cs.se.flashcards.model.DefaultFlashcardSeries;
import de.tud.cs.se.flashcards.model.Flashcard;
import de.tud.cs.se.flashcards.model.FlashcardSeries;
import de.tud.cs.st.constraints.NotNull;


/**
 * Some helper methods related to persisting a flashcard series.
 * <p>
 * <b><font color="red">Attention: Persistence is currently not handled properly! Do never use Java
 * Serialization for long term storage! </font></b>
 * </p>
 *
 * @author Michael Eichberg
 */
public final class Store {

    public static final @NotNull String FILE_ENDING = ".flashcards";

    private Store() {
        // prevent initialization
    }

    public static @NotNull FlashcardSeries openSeries(@NotNull File file) throws IOException {

        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(file))) {
            DefaultFlashcardSeries series = new DefaultFlashcardSeries();
            series.setNextCreationID(oin.readInt());
            int size = oin.readInt();
            for (int i = 0; i < size; i++) {
                Flashcard f = (Flashcard) oin.readObject();
                series.addCard(f);
            }
            return series;

        } catch (ClassNotFoundException e) {
            // The file did contain something unexpected; let's treat this as an IOException.
            throw new IOException(e);
        }
    }


    public static void saveSeries(@NotNull FlashcardSeries flashcardSeries, @NotNull File file)
            throws IOException {

        FlashcardSeries fs = flashcardSeries.getSourceModel();

        try (ObjectOutputStream oOut = new ObjectOutputStream(new FileOutputStream(file))) {
            oOut.write(flashcardSeries.getNextCreationID());
            oOut.writeInt(fs.getSize());
            for (int i = 0; i < fs.getSize(); i++) {
                oOut.writeObject(fs.getElementAt(i));
            }
        }
    }
}
