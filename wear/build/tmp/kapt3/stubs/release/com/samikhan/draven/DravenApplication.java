package com.samikhan.draven;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 \n2\u00020\u0001:\u0001\nB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\b\u001a\u00020\tH\u0016R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u000b"}, d2 = {"Lcom/samikhan/draven/DravenApplication;", "Landroid/app/Application;", "()V", "<set-?>", "Lcom/samikhan/draven/data/database/AppDatabase;", "database", "getDatabase", "()Lcom/samikhan/draven/data/database/AppDatabase;", "onCreate", "", "Companion", "wear_release"})
public final class DravenApplication extends android.app.Application {
    private com.samikhan.draven.data.database.AppDatabase database;
    private static com.samikhan.draven.DravenApplication instance;
    @org.jetbrains.annotations.NotNull
    public static final com.samikhan.draven.DravenApplication.Companion Companion = null;
    
    public DravenApplication() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.samikhan.draven.data.database.AppDatabase getDatabase() {
        return null;
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/samikhan/draven/DravenApplication$Companion;", "", "()V", "<set-?>", "Lcom/samikhan/draven/DravenApplication;", "instance", "getInstance", "()Lcom/samikhan/draven/DravenApplication;", "wear_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.samikhan.draven.DravenApplication getInstance() {
            return null;
        }
    }
}