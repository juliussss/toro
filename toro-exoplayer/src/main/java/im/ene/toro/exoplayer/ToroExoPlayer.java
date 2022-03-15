/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.toro.exoplayer;

import android.content.Context;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.BasePlayer;
import com.google.android.exoplayer2.DeviceInfo;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.PlayerMessage;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AuxEffectInfo;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ShuffleOrder;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.PriorityTaskManager;
import com.google.android.exoplayer2.video.VideoFrameMetadataListener;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.android.exoplayer2.video.spherical.CameraMotionListener;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.VolumeInfo;
import java.util.List;

import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * A custom {@link ExoPlayer} that also notify the change of Volume.
 *
 * @author eneim (2018/03/27).
 */
@SuppressWarnings("WeakerAccess") //
public class ToroExoPlayer extends BasePlayer implements ExoPlayer {

  protected ToroExoPlayer(
      Context context) {

    new ExoPlayer.Builder(context).build();

    //this(
    //    new ExoPlayer.Builder(context, renderersFactory)
    //        //.setUseLazyPreparation(useLazyPreparation)
    //        //.setClock(clock)
    //        //.setLooper(looper)
    //);
  }

  private ToroPlayer.VolumeChangeListeners listeners;

  public final void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    if (this.listeners == null) this.listeners = new ToroPlayer.VolumeChangeListeners();
    this.listeners.add(checkNotNull(listener));
  }

  public final void removeOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    if (this.listeners != null) this.listeners.remove(listener);
  }

  public final void clearOnVolumeChangeListener() {
    if (this.listeners != null) this.listeners.clear();
  }

  @CallSuper @Override public void setVolume(float audioVolume) {
    this.setVolumeInfo(new VolumeInfo(audioVolume == 0, audioVolume));
  }

  @Override public float getVolume() {
    return 0;
  }

  @Override public void clearVideoSurface() {

  }

  @Override public void clearVideoSurface(@Nullable Surface surface) {

  }

  @Override public void setVideoSurface(@Nullable Surface surface) {

  }

  @Override public void setVideoSurfaceHolder(@Nullable SurfaceHolder surfaceHolder) {

  }

  @Override public void clearVideoSurfaceHolder(@Nullable SurfaceHolder surfaceHolder) {

  }

  @Override public void setVideoSurfaceView(@Nullable SurfaceView surfaceView) {

  }

  @Override public void clearVideoSurfaceView(@Nullable SurfaceView surfaceView) {

  }

  @Override public void setVideoTextureView(@Nullable TextureView textureView) {

  }

  @Override public void clearVideoTextureView(@Nullable TextureView textureView) {

  }

  @Override public VideoSize getVideoSize() {
    return null;
  }

  @Override public List<Cue> getCurrentCues() {
    return null;
  }

  @Override public DeviceInfo getDeviceInfo() {
    return null;
  }

  @Override public int getDeviceVolume() {
    return 0;
  }

  @Override public boolean isDeviceMuted() {
    return false;
  }

  @Override public void setDeviceVolume(int volume) {

  }

  @Override public void increaseDeviceVolume() {

  }

  @Override public void decreaseDeviceVolume() {

  }

  @Override public void setDeviceMuted(boolean muted) {

  }

  private final VolumeInfo volumeInfo = new VolumeInfo(false, 1f);

  @SuppressWarnings("UnusedReturnValue")
  public final boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    boolean changed = !this.volumeInfo.equals(volumeInfo);
    if (changed) {
      this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
      this.setVolume(volumeInfo.isMute() ? 0 : volumeInfo.getVolume());
      if (listeners != null) {
        for (ToroPlayer.OnVolumeChangeListener listener : this.listeners) {
          listener.onVolumeChanged(volumeInfo);
        }
      }
    }

    return changed;
  }

  @SuppressWarnings("unused") @NonNull public final VolumeInfo getVolumeInfo() {
    return volumeInfo;
  }

  @Override public Looper getApplicationLooper() {
    return null;
  }

  @Override public void addListener(Listener listener) {

  }

  @Override public void removeListener(Listener listener) {

  }

  @Override public void setMediaItems(List<MediaItem> mediaItems, boolean resetPosition) {

  }

  @Override
  public void setMediaItems(List<MediaItem> mediaItems, int startIndex, long startPositionMs) {

  }

  @Override public void addMediaItems(int index, List<MediaItem> mediaItems) {

  }

  @Override public void moveMediaItems(int fromIndex, int toIndex, int newIndex) {

  }

  @Override public void removeMediaItems(int fromIndex, int toIndex) {

  }

  @Override public Commands getAvailableCommands() {
    return null;
  }

  @Override public void prepare() {

  }

  @Override public int getPlaybackState() {
    return Player.STATE_IDLE;
  }

  @Override public int getPlaybackSuppressionReason() {
    return Player.PLAYBACK_SUPPRESSION_REASON_NONE;
  }

  @Nullable @Override public ExoPlaybackException getPlayerError() {
    return null;
  }

  @Override public void setPlayWhenReady(boolean playWhenReady) {

  }

  @Override public boolean getPlayWhenReady() {
    return false;
  }

  @Override public void setRepeatMode(int repeatMode) {

  }

  @Override public int getRepeatMode() {
    return Player.REPEAT_MODE_OFF;
  }

  @Override public void setShuffleModeEnabled(boolean shuffleModeEnabled) {

  }

  @Override public boolean getShuffleModeEnabled() {
    return false;
  }

  @Override public boolean isLoading() {
    return false;
  }

  @Override public void seekTo(int mediaItemIndex, long positionMs) {

  }

  @Override public long getSeekBackIncrement() {
    return 0;
  }

  @Override public long getSeekForwardIncrement() {
    return 0;
  }

  @Override public long getMaxSeekToPreviousPosition() {
    return 0;
  }

  @Override public void setPlaybackParameters(@NonNull PlaybackParameters playbackParameters) {

  }

  @NonNull @Override public PlaybackParameters getPlaybackParameters() {
    return null;
  }

  @Override public void stop() {

  }

  @Override public void stop(boolean reset) {

  }

  @Override public void release() {

  }

  @Override public TrackGroupArray getCurrentTrackGroups() {
    return null;
  }

  @Override public TrackSelectionArray getCurrentTrackSelections() {
    return null;
  }

  @Override public TracksInfo getCurrentTracksInfo() {
    return null;
  }

  @Override public TrackSelectionParameters getTrackSelectionParameters() {
    return null;
  }

  @Override public void setTrackSelectionParameters(TrackSelectionParameters parameters) {

  }

  @Override public MediaMetadata getMediaMetadata() {
    return null;
  }

  @Override public MediaMetadata getPlaylistMetadata() {
    return null;
  }

  @Override public void setPlaylistMetadata(MediaMetadata mediaMetadata) {

  }

  @Override public Timeline getCurrentTimeline() {
    return null;
  }

  @Override public int getCurrentPeriodIndex() {
    return 0;
  }

  @Override public int getCurrentMediaItemIndex() {
    return 0;
  }

  @Override public long getDuration() {
    return 0;
  }

  @Override public long getCurrentPosition() {
    return 0;
  }

  @Override public long getBufferedPosition() {
    return 0;
  }

  @Override public long getTotalBufferedDuration() {
    return 0;
  }

  @Override public boolean isPlayingAd() {
    return false;
  }

  @Override public int getCurrentAdGroupIndex() {
    return 0;
  }

  @Override public int getCurrentAdIndexInAdGroup() {
    return 0;
  }

  @Override public long getContentPosition() {
    return 0;
  }

  @Override public long getContentBufferedPosition() {
    return 0;
  }

  @Override public AudioAttributes getAudioAttributes() {
    return null;
  }

  @Nullable @Override public AudioComponent getAudioComponent() {
    return null;
  }

  @Nullable @Override public VideoComponent getVideoComponent() {
    return null;
  }

  @Nullable @Override public TextComponent getTextComponent() {
    return null;
  }

  @Nullable @Override public DeviceComponent getDeviceComponent() {
    return null;
  }

  @Override public void addAudioOffloadListener(AudioOffloadListener listener) {

  }

  @Override public void removeAudioOffloadListener(AudioOffloadListener listener) {

  }

  @Override public AnalyticsCollector getAnalyticsCollector() {
    return null;
  }

  @Override public void addAnalyticsListener(AnalyticsListener listener) {

  }

  @Override public void removeAnalyticsListener(AnalyticsListener listener) {

  }

  @Override public int getRendererCount() {
    return 0;
  }

  @Override public int getRendererType(int index) {
    return 0;
  }

  @Override public Renderer getRenderer(int index) {
    return null;
  }

  @Nullable @Override public TrackSelector getTrackSelector() {
    return null;
  }

  @Override public Looper getPlaybackLooper() {
    return null;
  }

  @Override public Clock getClock() {
    return null;
  }

  @Override public void retry() {

  }

  @Override public void prepare(MediaSource mediaSource) {

  }

  @Override
  public void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState) {

  }

  @Override public void setMediaSources(List<MediaSource> mediaSources) {

  }

  @Override public void setMediaSources(List<MediaSource> mediaSources, boolean resetPosition) {

  }

  @Override public void setMediaSources(List<MediaSource> mediaSources, int startMediaItemIndex,
      long startPositionMs) {

  }

  @Override public void setMediaSource(MediaSource mediaSource) {

  }

  @Override public void setMediaSource(MediaSource mediaSource, long startPositionMs) {

  }

  @Override public void setMediaSource(MediaSource mediaSource, boolean resetPosition) {

  }

  @Override public void addMediaSource(MediaSource mediaSource) {

  }

  @Override public void addMediaSource(int index, MediaSource mediaSource) {

  }

  @Override public void addMediaSources(List<MediaSource> mediaSources) {

  }

  @Override public void addMediaSources(int index, List<MediaSource> mediaSources) {

  }

  @Override public void setShuffleOrder(ShuffleOrder shuffleOrder) {

  }

  @Override
  public void setAudioAttributes(AudioAttributes audioAttributes, boolean handleAudioFocus) {

  }

  @Override public void setAudioSessionId(int audioSessionId) {

  }

  @Override public int getAudioSessionId() {
    return 0;
  }

  @Override public void setAuxEffectInfo(AuxEffectInfo auxEffectInfo) {

  }

  @Override public void clearAuxEffectInfo() {

  }

  @Override public void setSkipSilenceEnabled(boolean skipSilenceEnabled) {

  }

  @Override public boolean getSkipSilenceEnabled() {
    return false;
  }

  @Override public void setVideoScalingMode(int videoScalingMode) {

  }

  @Override public int getVideoScalingMode() {
    return 0;
  }

  @Override public void setVideoChangeFrameRateStrategy(int videoChangeFrameRateStrategy) {

  }

  @Override public int getVideoChangeFrameRateStrategy() {
    return 0;
  }

  @Override public void setVideoFrameMetadataListener(VideoFrameMetadataListener listener) {

  }

  @Override public void clearVideoFrameMetadataListener(VideoFrameMetadataListener listener) {

  }

  @Override public void setCameraMotionListener(CameraMotionListener listener) {

  }

  @Override public void clearCameraMotionListener(CameraMotionListener listener) {

  }

  @Override public PlayerMessage createMessage(PlayerMessage.Target target) {
    return null;
  }

  @Override public void setSeekParameters(@Nullable SeekParameters seekParameters) {

  }

  @Override public SeekParameters getSeekParameters() {
    return null;
  }

  @Override public void setForegroundMode(boolean foregroundMode) {

  }

  @Override public void setPauseAtEndOfMediaItems(boolean pauseAtEndOfMediaItems) {

  }

  @Override public boolean getPauseAtEndOfMediaItems() {
    return false;
  }

  @Nullable @Override public Format getAudioFormat() {
    return null;
  }

  @Nullable @Override public Format getVideoFormat() {
    return null;
  }

  @Nullable @Override public DecoderCounters getAudioDecoderCounters() {
    return null;
  }

  @Nullable @Override public DecoderCounters getVideoDecoderCounters() {
    return null;
  }

  @Override public void setHandleAudioBecomingNoisy(boolean handleAudioBecomingNoisy) {

  }

  @Override public void setHandleWakeLock(boolean handleWakeLock) {

  }

  @Override public void setWakeMode(int wakeMode) {

  }

  @Override public void setPriorityTaskManager(@Nullable PriorityTaskManager priorityTaskManager) {

  }

  @Override public void experimentalSetOffloadSchedulingEnabled(boolean offloadSchedulingEnabled) {

  }

  @Override public boolean experimentalIsSleepingForOffload() {
    return false;
  }
}
