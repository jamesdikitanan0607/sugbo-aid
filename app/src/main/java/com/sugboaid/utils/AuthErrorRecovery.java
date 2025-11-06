package com.sugboaid.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Error recovery mechanism for authentication operations
 * Provides automatic retry logic with exponential backoff and circuit breaker pattern
 */
public class AuthErrorRecovery {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY = 1000L; // 1 second
    private static final long MAX_RETRY_DELAY = 10000L; // 10 seconds
    private static final double BACKOFF_MULTIPLIER = 2.0;

    /**
     * Recovery strategy interface
     */
    public interface RecoveryStrategy {
        void execute();
        void onSuccess();
        void onFailure(AuthErrorHandler.ErrorResult error);
        void onMaxRetriesReached();
    }

    /**
     * Recovery context containing retry information
     */
    public static class RecoveryContext {
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        private final long startTime = System.currentTimeMillis();
        private final String operationName;
        private boolean isRecovering = false;

        public RecoveryContext(@NonNull String operationName) {
            this.operationName = operationName;
        }

        public int getAttemptCount() {
            return attemptCount.get();
        }

        public int incrementAttemptCount() {
            return attemptCount.incrementAndGet();
        }

        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }

        public String getOperationName() {
            return operationName;
        }

        public boolean isRecovering() {
            return isRecovering;
        }

        public void setRecovering(boolean recovering) {
            isRecovering = recovering;
        }

        public void reset() {
            attemptCount.set(0);
            isRecovering = false;
        }
    }

    /**
     * Circuit breaker for preventing cascading failures
     */
    public static class CircuitBreaker {
        private enum State {
            CLOSED,    // Normal operation
            OPEN,      // Failing fast
            HALF_OPEN  // Testing if service recovered
        }

        private State state = State.CLOSED;
        private int failureCount = 0;
        private long lastFailureTime = 0;
        private final int failureThreshold;
        private final long timeout;

        public CircuitBreaker(int failureThreshold, long timeout) {
            this.failureThreshold = failureThreshold;
            this.timeout = timeout;
        }

        public boolean canExecute() {
            if (state == State.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime >= timeout) {
                    state = State.HALF_OPEN;
                    return true;
                }
                return false;
            }
            return true;
        }

        public void onSuccess() {
            failureCount = 0;
            state = State.CLOSED;
        }

        public void onFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
            }
        }

        public State getState() {
            return state;
        }

        public int getFailureCount() {
            return failureCount;
        }
    }

    private final Context context;
    private final Handler mainHandler;
    private final CircuitBreaker circuitBreaker;

    public AuthErrorRecovery(@NonNull Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.circuitBreaker = new CircuitBreaker(5, 30000L); // 5 failures, 30 second timeout
    }

    /**
     * Execute operation with automatic retry and recovery
     * @param strategy Recovery strategy to execute
     * @param recoveryContext Context for tracking recovery attempts
     */
    public void executeWithRecovery(@NonNull RecoveryStrategy strategy, @NonNull RecoveryContext recoveryContext) {
        if (!circuitBreaker.canExecute()) {
            // Circuit breaker is open, fail fast
            AuthErrorHandler.ErrorResult error = new AuthErrorHandler.ErrorResult.Builder()
                    .setType(AuthErrorHandler.ErrorType.UNKNOWN_ERROR)
                    .setSeverity(AuthErrorHandler.ErrorSeverity.HIGH)
                    .setMessage("Service temporarily unavailable")
                    .setUserMessage("Service is temporarily unavailable. Please try again later.")
                    .setCanRetry(false)
                    .build();
            strategy.onFailure(error);
            return;
        }

        recoveryContext.setRecovering(true);
        
        try {
            strategy.execute();
            
            // If we reach here, operation was successful
            circuitBreaker.onSuccess();
            recoveryContext.reset();
            strategy.onSuccess();
            
        } catch (Exception e) {
            handleOperationFailure(e, strategy, recoveryContext);
        }
    }

    /**
     * Handle operation failure with retry logic
     */
    private void handleOperationFailure(@NonNull Exception exception, @NonNull RecoveryStrategy strategy, 
                                      @NonNull RecoveryContext recoveryContext) {
        circuitBreaker.onFailure();
        
        int currentAttempt = recoveryContext.incrementAttemptCount();
        
        if (currentAttempt >= MAX_RETRY_ATTEMPTS) {
            // Max retries reached
            recoveryContext.setRecovering(false);
            strategy.onMaxRetriesReached();
            return;
        }

        // Determine if error is recoverable
        AuthErrorHandler.ErrorResult errorResult = AuthErrorHandler.fromException(exception, null);
        
        if (!isRecoverableError(errorResult)) {
            // Non-recoverable error, don't retry
            recoveryContext.setRecovering(false);
            strategy.onFailure(errorResult);
            return;
        }

        // Calculate retry delay with exponential backoff
        long retryDelay = calculateRetryDelay(currentAttempt);
        
        // Schedule retry
        mainHandler.postDelayed(() -> {
            if (recoveryContext.isRecovering()) {
                executeWithRecovery(strategy, recoveryContext);
            }
        }, retryDelay);
    }

    /**
     * Calculate retry delay with exponential backoff
     */
    private long calculateRetryDelay(int attemptNumber) {
        long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(BACKOFF_MULTIPLIER, attemptNumber - 1));
        return Math.min(delay, MAX_RETRY_DELAY);
    }

    /**
     * Determine if error is recoverable
     */
    private boolean isRecoverableError(@NonNull AuthErrorHandler.ErrorResult error) {
        switch (error.getType()) {
            case NETWORK_ERROR:
            case UNKNOWN_ERROR:
                return true;
            case VALIDATION_ERROR:
            case AUTHENTICATION_ERROR:
                return false;
            case SESSION_ERROR:
                return error.getSeverity() != AuthErrorHandler.ErrorSeverity.CRITICAL;
            default:
                return false;
        }
    }

    /**
     * Create recovery strategy for login operation
     * @param email User email
     * @param password User password
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     * @param onMaxRetries Max retries callback
     * @return RecoveryStrategy for login
     */
    public static RecoveryStrategy createLoginRecoveryStrategy(
            @NonNull String email, 
            @NonNull String password,
            @NonNull Runnable onSuccess,
            @NonNull java.util.function.Consumer<AuthErrorHandler.ErrorResult> onFailure,
            @NonNull Runnable onMaxRetries) {
        
        return new RecoveryStrategy() {
            @Override
            public void execute() {
                // This would be implemented by the calling code
                // For now, we'll throw an exception to simulate failure
                throw new RuntimeException("Simulated login failure for recovery testing");
            }

            @Override
            public void onSuccess() {
                onSuccess.run();
            }

            @Override
            public void onFailure(AuthErrorHandler.ErrorResult error) {
                onFailure.accept(error);
            }

            @Override
            public void onMaxRetriesReached() {
                onMaxRetries.run();
            }
        };
    }

    /**
     * Create recovery strategy for signup operation
     * @param name User name
     * @param email User email
     * @param password User password
     * @param confirmPassword Password confirmation
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     * @param onMaxRetries Max retries callback
     * @return RecoveryStrategy for signup
     */
    public static RecoveryStrategy createSignupRecoveryStrategy(
            @NonNull String name,
            @NonNull String email, 
            @NonNull String password,
            @NonNull String confirmPassword,
            @NonNull Runnable onSuccess,
            @NonNull java.util.function.Consumer<AuthErrorHandler.ErrorResult> onFailure,
            @NonNull Runnable onMaxRetries) {
        
        return new RecoveryStrategy() {
            @Override
            public void execute() {
                // This would be implemented by the calling code
                throw new RuntimeException("Simulated signup failure for recovery testing");
            }

            @Override
            public void onSuccess() {
                onSuccess.run();
            }

            @Override
            public void onFailure(AuthErrorHandler.ErrorResult error) {
                onFailure.accept(error);
            }

            @Override
            public void onMaxRetriesReached() {
                onMaxRetries.run();
            }
        };
    }

    /**
     * Cancel ongoing recovery operation
     * @param recoveryContext Context to cancel
     */
    public void cancelRecovery(@NonNull RecoveryContext recoveryContext) {
        recoveryContext.setRecovering(false);
    }

    /**
     * Get circuit breaker status
     * @return Current circuit breaker state
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }

    /**
     * Reset circuit breaker
     */
    public void resetCircuitBreaker() {
        circuitBreaker.onSuccess();
    }

    /**
     * Get retry delay for attempt number
     * @param attemptNumber Attempt number (1-based)
     * @return Delay in milliseconds
     */
    public static long getRetryDelay(int attemptNumber) {
        long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(BACKOFF_MULTIPLIER, attemptNumber - 1));
        return Math.min(delay, MAX_RETRY_DELAY);
    }

    /**
     * Check if operation should be retried based on error
     * @param error Error result
     * @param attemptCount Current attempt count
     * @return true if should retry, false otherwise
     */
    public static boolean shouldRetry(@NonNull AuthErrorHandler.ErrorResult error, int attemptCount) {
        if (attemptCount >= MAX_RETRY_ATTEMPTS) {
            return false;
        }

        switch (error.getType()) {
            case NETWORK_ERROR:
            case UNKNOWN_ERROR:
                return true;
            case SESSION_ERROR:
                return error.getSeverity() != AuthErrorHandler.ErrorSeverity.CRITICAL;
            case VALIDATION_ERROR:
            case AUTHENTICATION_ERROR:
            default:
                return false;
        }
    }
}