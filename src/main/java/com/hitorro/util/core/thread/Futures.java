/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.thread;

import com.hitorro.util.core.Timer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Futures {
    public static Object[] await(long accumulativeMaxWait, Future... futures) throws InterruptedException, ExecutionException, TimeoutException {
        long waitTime = 100;
        Object arr[] = new Object[futures.length];
        Timer t = new Timer();
        for (int i = 0; i < futures.length; i++) {
            long s = t.reset();
            arr[i] = futures[i].get(accumulativeMaxWait, TimeUnit.MILLISECONDS);
            accumulativeMaxWait -= t.stop();
        }
        return arr;
    }

    /*
    void demo() {
        CompletableFuture.supplyAsync(App::getFileName, JAVA_FX)
                .thenComposeAsync(Futures::readDiskNonBlocking)
                .thenComposeAsync(App::writeLanNonBlocking)
                .thenRunAsync(App::notifyUser, JAVA_FX);
    }

    static CompletableFuture<String> readDiskNonBlocking(Path fileName) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        try {
            AsynchronousFileChannel ch = AsynchronousFileChannel.open(fileName);
            ByteBuffer b = ByteBuffer.allocate(...);
            ch.read(b, 0L, cf, new CompletionHandler<Integer, CompletableFuture<String>>() {
                @Override public void completed(Integer l, CompletableFuture<String> a) {
                    a.complete(...);
                }

                @Override public void failed(Throwable t, final CompletableFuture<String> a) {
                    a.completeExceptionally(t);
                }
            });
        } catch (IOException e) {
            completableFuture.completeExceptionally(e);
        }
        return cf;
    }

    static CompletableFuture<Void> writeLanNonBlocking(String fileContent) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
  ...
        return cf;
    }
    */
}
