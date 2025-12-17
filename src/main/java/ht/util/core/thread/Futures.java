package ht.util.core.thread;

import ht.util.core.Timer;

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
