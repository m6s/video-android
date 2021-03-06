package info.mschmitt.video.mediaplayer;

import android.databinding.Bindable;
import android.databinding.PropertyChangeRegistry;

import java.util.concurrent.TimeUnit;

import info.mschmitt.video.BR;
import info.mschmitt.video.commons.DataBindingObservable;
import rx.Completable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Matthias Schmitt
 */

public class MediaPlayerViewModel implements DataBindingObservable {
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int INITIAL_DELAY_MILLIS = 1000;
    private final PropertyChangeRegistry propertyChangeRegistry = new PropertyChangeRegistry();
    private final Router router;
    @Bindable
    public boolean controlsVisible = true;
    private boolean firstView = true;
    private Subscription delayedToggleControlsSubscription;

    public MediaPlayerViewModel(Router router) {
        this.router = router;
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    public boolean onControlTouch() {
        scheduleToggleControls(MediaPlayerViewModel.AUTO_HIDE_DELAY_MILLIS);
        return false;
    }

    public void onContentClick() {
        scheduleToggleControls(0);
    }

    public void onSystemFullscreenChanged(boolean fullscreen) {
        if (fullscreen) {
            return;
        }
        scheduleToggleControls(0);
    }

    public void onPlayPauseClick() {
    }

    private void scheduleToggleControls(int delayMillis) {
        if (delayedToggleControlsSubscription != null) {
            delayedToggleControlsSubscription.unsubscribe();
        }
        delayedToggleControlsSubscription = Completable.complete()
                .delay(delayMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(this::toggleControls);
    }

    private void setControlsVisible(boolean controlsVisible) {
        this.controlsVisible = controlsVisible;
        propertyChangeRegistry.notifyChange(this, BR.controlsVisible);
    }

    private void toggleControls() {
        setControlsVisible(!controlsVisible);
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {
        propertyChangeRegistry.add(onPropertyChangedCallback);
    }

    @Override
    public void removeOnPropertyChangedCallback(
            OnPropertyChangedCallback onPropertyChangedCallback) {
        propertyChangeRegistry.remove(onPropertyChangedCallback);
    }

    public void onPause() {
    }

    public void onResume() {
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if (!controlsVisible || firstView) {
            setControlsVisible(true);
            scheduleToggleControls(INITIAL_DELAY_MILLIS);
        }
        firstView = false;
    }

    public interface Router {
        void showFileChooser();
    }
}
