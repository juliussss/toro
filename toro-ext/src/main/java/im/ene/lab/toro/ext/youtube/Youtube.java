/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.lab.toro.ext.youtube;

import android.text.TextUtils;

/**
 * Created by eneim on 6/6/16.
 */
public final class Youtube {

  static String API_KEY = null;

  public static void setApiKey(String key) {
    if (key == null || TextUtils.isEmpty(key)) {
      throw new IllegalArgumentException("Invalid Youtube API Key. "
          + "Please provide a valid one through Manifest's meta-data of name: 'Youtube.API_KEY'");
    }

    API_KEY = key;
  }
}
