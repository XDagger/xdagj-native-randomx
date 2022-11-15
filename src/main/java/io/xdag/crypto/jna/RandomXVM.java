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
