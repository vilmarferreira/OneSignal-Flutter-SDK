package com.onesignal.flutter;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.onesignal.OneSignal;
import com.onesignal.OneSignal.GetTagsHandler;
import com.onesignal.OneSignal.ChangeTagsUpdateHandler;
import com.onesignal.OneSignal.SendTagsError;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

/**
 * Created by bradhesse on 7/17/18.
 */

public class OneSignalTagsController implements MethodCallHandler, ChangeTagsUpdateHandler, GetTagsHandler {
    private MethodChannel channel;
    private Result sendTagsResult;
    private Result getTagsResult;

    public static void registerWith(Registrar registrar) {
        OneSignalTagsController controller = new OneSignalTagsController();
        controller.channel = new MethodChannel(registrar.messenger(), "OneSignal#tags");
        controller.channel.setMethodCallHandler(controller);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.contentEquals("OneSignal#getTags")) {
            getTagsResult = result;
            OneSignal.getTags(this);
        } else if (call.method.contentEquals("OneSignal#sendTags")) {
            sendTagsResult = result;
            OneSignal.sendTags(new JSONObject((Map<String, Object>)call.arguments), this);
        } else if (call.method.contentEquals("OneSignal#deleteTags")
                ) {
            sendTagsResult = result;
            OneSignal.deleteTags((List<String>)call.arguments, this);
        }
    }

    @Override
    public void onSuccess(JSONObject tags) {
        if (sendTagsResult != null) {
            try {
                sendTagsResult.success(OneSignalSerializer.convertJSONObjectToHashMap(tags));
            } catch (JSONException exception) {
                sendTagsResult.error("onesignal", "Encountered an error serializing tags into hashmap: " + exception.getMessage() + "\n" + exception.getStackTrace(), null);
            }

            sendTagsResult = null;
        }
    }

    @Override
    public void onFailure(SendTagsError error) {
        if (sendTagsResult != null) {
            sendTagsResult.error("onesignal", "Encountered an error updating tags (" + String.valueOf(error.getCode()) + "): " + error.getMessage(), null);
            sendTagsResult = null;
        }
    }

    @Override
    public void tagsAvailable(JSONObject jsonObject) {
        if (getTagsResult != null) {
            try {
                getTagsResult.success(OneSignalSerializer.convertJSONObjectToHashMap(jsonObject));
            } catch (JSONException exception) {
                getTagsResult.error("onesignal", "Encountered an error serializing tags into hashmap: " + exception.getMessage() + "\n" + exception.getStackTrace(), null);
            }

            getTagsResult = null;
        }
    }
}
