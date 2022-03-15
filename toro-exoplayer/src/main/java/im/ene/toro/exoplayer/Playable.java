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

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.DeviceInfo;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.video.VideoSize;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.RemoveIn;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Define an interface to control a playback, specific for {@link ExoPlayer} and {@link StyledPlayerView}.
 *
 * This interface is designed to be reused across Config change. Implementation must not hold any
 * strong reference to Activity, and if it supports any kind of that, make sure to implicitly clean
 * it up.
 *
 * @author eneim
 * @since 3.4.0
 */

@SuppressWarnings("unused") //
public interface Playable {

  /**
   * Prepare the resource for a {@link ExoPlayer}. This method should:
   * - Request for new {@link ExoPlayer} instance if there is not a usable one.
   * - Configure {@link EventListener} for it.
   * - If there is non-trivial PlaybackInfo, update it to the ExoPlayer.
   * - If client request to prepare MediaSource, then prepare it.
   *
   * This method must be called before {@link #setPlayerView(StyledPlayerView)}.
   *
   * @param prepareSource if {@code true}, also prepare the MediaSource when preparing the Player,
   * if {@code false} just do nothing for the MediaSource.
   */
  void prepare(boolean prepareSource);

  /**
   * Set the {@link StyledPlayerView} for this Playable. It is expected that a playback doesn't require a
   * UI, so this setup is optional. But it must be called after the ExoPlayer is prepared,
   * that is after {@link #prepare(boolean)} and before {@link #release()}.
   *
   * Changing the StyledPlayerView during playback is expected, though not always recommended, especially
   * on old Devices with low Android API.
   *
   * @param playerView the StyledPlayerView to set to the ExoPlayer.
   */
  void setPlayerView(@Nullable StyledPlayerView playerView);

  /**
   * Get current {@link StyledPlayerView} of this Playable.
   *
   * @return current StyledPlayerView instance of this Playable.
   */
  @Nullable StyledPlayerView getPlayerView();

  /**
   * Start the playback. If the {@link MediaSource} is not prepared, then also prepare it.
   */
  void play();

  /**
   * Pause the playback.
   */
  void pause();

  /**
   * Reset all resource, so that the playback can start all over again. This is to cleanup the
   * playback for reuse. The ExoPlayer instance must be still usable without calling
   * {@link #prepare(boolean)}.
   */
  void reset();

  /**
   * Release all resource. After this, the ExoPlayer is released to the Player pool and the
   * Playable must call {@link #prepare(boolean)} again to use it again.
   */
  void release();

  /**
   * Get current {@link PlaybackInfo} of the playback.
   *
   * @return current PlaybackInfo of the playback.
   */
  @NonNull PlaybackInfo getPlaybackInfo();

  /**
   * Set the custom {@link PlaybackInfo} for this playback. This could suggest a seek.
   *
   * @param playbackInfo the PlaybackInfo to set for this playback.
   */
  void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo);

  /**
   * Add a new {@link EventListener} to this Playable. As calling {@link #prepare(boolean)} also
   * triggers some internal events, this method should be called before {@link #prepare(boolean)} so
   * that Client could received them all.
   *
   * @param listener the EventListener to add, must be not {@code null}.
   */
  void addEventListener(@NonNull EventListener listener);

  /**
   * Remove an {@link EventListener} from this Playable.
   *
   * @param listener the EventListener to be removed. If null, nothing happens.
   */
  void removeEventListener(EventListener listener);

  /**
   * !This must only work if the Player in use is a {@link ToroExoPlayer}.
   */
  void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener);

  void removeOnVolumeChangeListener(@Nullable ToroPlayer.OnVolumeChangeListener listener);

  /**
   * Check if current Playable is playing or not.
   *
   * @return {@code true} if this Playable is playing, {@code false} otherwise.
   */
  boolean isPlaying();

  /**
   * Change the volume of current playback.
   *
   * @param volume the volume value to be set. Must be a {@code float} of range from 0 to 1.
   * @deprecated use {@link #setVolumeInfo(VolumeInfo)} instead.
   */
  @RemoveIn(version = "3.6.0") @Deprecated  //
  void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume);

  /**
   * Obtain current volume value. The returned value is a {@code float} of range from 0 to 1.
   *
   * @return current volume value.
   * @deprecated use {@link #getVolumeInfo()} instead.
   */
  @RemoveIn(version = "3.6.0") @Deprecated  //
  @FloatRange(from = 0.0, to = 1.0) float getVolume();

  /**
   * Update playback's volume.
   *
   * @param volumeInfo the {@link VolumeInfo} to update to.
   * @return {@code true} if current Volume info is updated, {@code false} otherwise.
   */
  boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo);

  /**
   * Get current {@link VolumeInfo}.
   */
  @NonNull VolumeInfo getVolumeInfo();

  /**
   * Same as {@link Player#setPlaybackParameters(PlaybackParameters)}
   */
  void setParameters(@Nullable PlaybackParameters parameters);

  /**
   * Same as {@link Player#getPlaybackParameters()}
   */
  @Nullable PlaybackParameters getParameters();

  void addErrorListener(@NonNull ToroPlayer.OnErrorListener listener);

  void removeErrorListener(@Nullable ToroPlayer.OnErrorListener listener);

  // Combine necessary interfaces.
  interface EventListener extends Player.Listener, TextOutput, MetadataOutput {

    @Override default void onEvents(Player player, Player.Events events) {
      Player.Listener.super.onEvents(player, events);
    }

    @Override default void onTimelineChanged(Timeline timeline, int reason) {
      Player.Listener.super.onTimelineChanged(timeline, reason);
    }

    @Override default void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
      Player.Listener.super.onMediaItemTransition(mediaItem, reason);
    }

    @Override default void onTracksInfoChanged(TracksInfo tracksInfo) {
      Player.Listener.super.onTracksInfoChanged(tracksInfo);
    }

    @Override default void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
      Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
    }

    @Override default void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
      Player.Listener.super.onPlaylistMetadataChanged(mediaMetadata);
    }

    @Override default void onIsLoadingChanged(boolean isLoading) {
      Player.Listener.super.onIsLoadingChanged(isLoading);
    }

    @Override default void onAvailableCommandsChanged(Player.Commands availableCommands) {
      Player.Listener.super.onAvailableCommandsChanged(availableCommands);
    }

    @Override default void onTrackSelectionParametersChanged(TrackSelectionParameters parameters) {
      Player.Listener.super.onTrackSelectionParametersChanged(parameters);
    }

    @Override default void onPlaybackStateChanged(int playbackState) {
      Player.Listener.super.onPlaybackStateChanged(playbackState);
    }

    @Override default void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
      Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
    }

    @Override default void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {
      Player.Listener.super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason);
    }

    @Override default void onIsPlayingChanged(boolean isPlaying) {
      Player.Listener.super.onIsPlayingChanged(isPlaying);
    }

    @Override default void onRepeatModeChanged(int repeatMode) {
      Player.Listener.super.onRepeatModeChanged(repeatMode);
    }

    @Override default void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      Player.Listener.super.onShuffleModeEnabledChanged(shuffleModeEnabled);
    }

    @Override default void onPlayerError(PlaybackException error) {
      Player.Listener.super.onPlayerError(error);
    }

    @Override default void onPlayerErrorChanged(@Nullable PlaybackException error) {
      Player.Listener.super.onPlayerErrorChanged(error);
    }

    @Override default void onPositionDiscontinuity(Player.PositionInfo oldPosition,
        Player.PositionInfo newPosition, int reason) {
      Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
    }

    @Override default void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      Player.Listener.super.onPlaybackParametersChanged(playbackParameters);
    }

    @Override default void onSeekBackIncrementChanged(long seekBackIncrementMs) {
      Player.Listener.super.onSeekBackIncrementChanged(seekBackIncrementMs);
    }

    @Override default void onSeekForwardIncrementChanged(long seekForwardIncrementMs) {
      Player.Listener.super.onSeekForwardIncrementChanged(seekForwardIncrementMs);
    }

    @Override default void onMaxSeekToPreviousPositionChanged(long maxSeekToPreviousPositionMs) {
      Player.Listener.super.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs);
    }

    @Override default void onAudioSessionIdChanged(int audioSessionId) {
      Player.Listener.super.onAudioSessionIdChanged(audioSessionId);
    }

    @Override default void onAudioAttributesChanged(AudioAttributes audioAttributes) {
      Player.Listener.super.onAudioAttributesChanged(audioAttributes);
    }

    @Override default void onVolumeChanged(float volume) {
      Player.Listener.super.onVolumeChanged(volume);
    }

    @Override default void onSkipSilenceEnabledChanged(boolean skipSilenceEnabled) {
      Player.Listener.super.onSkipSilenceEnabledChanged(skipSilenceEnabled);
    }

    @Override default void onDeviceInfoChanged(DeviceInfo deviceInfo) {
      Player.Listener.super.onDeviceInfoChanged(deviceInfo);
    }

    @Override default void onDeviceVolumeChanged(int volume, boolean muted) {
      Player.Listener.super.onDeviceVolumeChanged(volume, muted);
    }

    @Override default void onVideoSizeChanged(VideoSize videoSize) {
      Player.Listener.super.onVideoSizeChanged(videoSize);
    }

    @Override default void onSurfaceSizeChanged(int width, int height) {
      Player.Listener.super.onSurfaceSizeChanged(width, height);
    }

    @Override default void onRenderedFirstFrame() {
      Player.Listener.super.onRenderedFirstFrame();
    }

    @Override default void onCues(List<Cue> cues) {
      Player.Listener.super.onCues(cues);
    }

    @Override default void onMetadata(Metadata metadata) {
      Player.Listener.super.onMetadata(metadata);
    }
  }

  /** Default empty implementation */
  class DefaultEventListener implements EventListener {

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override public void onLoadingChanged(boolean isLoading) {

    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override public void onRepeatModeChanged(int repeatMode) {

    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override public void onPlayerError(PlaybackException error) {
      EventListener.super.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {

    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override public void onSeekProcessed() {

    }

    @Override public void onVideoSizeChanged(VideoSize videoSize) {
      EventListener.super.onVideoSizeChanged(videoSize);
    }

    @Override
    public void onSurfaceSizeChanged(int width, int height) {

    }

    @Override public void onRenderedFirstFrame() {

    }

    @Override public void onCues(List<Cue> cues) {

    }

    @Override public void onMetadata(Metadata metadata) {

    }
  }

  /** List of EventListener */
  class EventListeners extends CopyOnWriteArraySet<EventListener> implements EventListener {

    EventListeners() {
    }

    @Override public void onVideoSizeChanged(VideoSize videoSize) {
      EventListener.super.onVideoSizeChanged(videoSize);
    }

    @Override
    public void onSurfaceSizeChanged(int width, int height) {
      for (EventListener eventListener : this) {
        eventListener.onSurfaceSizeChanged(width, height);
      }
    }

    @Override public void onRenderedFirstFrame() {
      for (EventListener eventListener : this) {
        eventListener.onRenderedFirstFrame();
      }
    }

    @Override public void onTimelineChanged(Timeline timeline, int reason) {
      EventListener.super.onTimelineChanged(timeline, reason);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      for (EventListener eventListener : this) {
        eventListener.onTracksChanged(trackGroups, trackSelections);
      }
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      for (EventListener eventListener : this) {
        eventListener.onLoadingChanged(isLoading);
      }
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      for (EventListener eventListener : this) {
        eventListener.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onRepeatModeChanged(int repeatMode) {
      for (EventListener eventListener : this) {
        eventListener.onRepeatModeChanged(repeatMode);
      }
    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      for (EventListener eventListener : this) {
        eventListener.onShuffleModeEnabledChanged(shuffleModeEnabled);
      }
    }

    @Override public void onPlayerError(PlaybackException error) {
      EventListener.super.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {
      for (EventListener eventListener : this) {
        eventListener.onPositionDiscontinuity(reason);
      }
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      for (EventListener eventListener : this) {
        eventListener.onPlaybackParametersChanged(playbackParameters);
      }
    }

    @Override public void onCues(List<Cue> cues) {
      for (EventListener eventListener : this) {
        eventListener.onCues(cues);
      }
    }

    @Override public void onMetadata(Metadata metadata) {
      for (EventListener eventListener : this) {
        eventListener.onMetadata(metadata);
      }
    }
  }
}
