/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.xdag.crypto.jna;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class RandomXVM {

    PointerByReference pointer;
    RandomXWrapper parent;

    RandomXVM(PointerByReference pointer, RandomXWrapper parent) {
        this.pointer = pointer;
        this.parent = parent;
    }

    /**
     * Calculate hash of given message
     * @param message the message to get the hash of
     * @return the resulting hash
     */
    public synchronized byte[] getHash(byte[] message) {

        Pointer msgPointer = new Memory(message.length);
        msgPointer.write(0, message, 0, message.length);

        Pointer hashPointer = new Memory(RandomXWrapper.HASH_SIZE);
        RandomXLib.INSTANCE.randomx_calculate_hash(pointer, msgPointer, new NativeSize(message.length), hashPointer);

        byte[] hash = hashPointer.getByteArray(0, RandomXWrapper.HASH_SIZE);

        msgPointer.clear(message.length);
        hashPointer.clear(RandomXWrapper.HASH_SIZE);

        return hash;
    }

    protected PointerByReference getPointer() {
        return pointer;
    }

    /**
     * Destroy this VM
     */
    public void destroy() {
        RandomXLib.INSTANCE.randomx_destroy_vm(pointer);
        parent.vms.remove(this);
    }

}
