package com.ihorshein.cordova.plugins;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.provider.CallLog;
import android.util.Log;
import static android.Manifest.permission.WRITE_CALL_LOG;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Cordova plugin that allows the application to write the user's call log data.
 */
public class CallLogWrite extends CordovaPlugin
{
  private static final String LOG_TAG = "CallLogWrite";
  private static final int WRITE_CALL_LOG_REQ_CODE = 0;

  private String action;
  private JSONArray args;
  private CallbackContext callbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
  {
    this.action = action;
    this.args = args;
    this.callbackContext = callbackContext;

    // Whether the application has permission or not (received from the client).
    Boolean hasPermissionEx = args.optBoolean(0);

    // The actual state of the permit (received from the system).
    Boolean hasPermissionIn = cordova.hasPermission(WRITE_CALL_LOG);

    if(hasPermissionEx || hasPermissionIn)
    {
      if(!hasPermissionIn)
      {
        Log.d(LOG_TAG, "Permission denied");
        callbackContext.sendPluginResult(new PluginResult(Status.ERROR, "permission-access"));

        return false;
      }

      return _actionSwitch();
    }
    else
    {
      // `onRequestPermissionResult` will be called.
      cordova.requestPermission(this, WRITE_CALL_LOG_REQ_CODE, WRITE_CALL_LOG);
    }

    return true;
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException
  {
    for(int r : grantResults)
    {
      if(r == PackageManager.PERMISSION_DENIED)
      {
        Log.d(LOG_TAG, "Permission denied");
        callbackContext.sendPluginResult(new PluginResult(Status.ERROR, "permission-access"));

        return;
      }
    }

    if(requestCode == WRITE_CALL_LOG_REQ_CODE)
      _actionSwitch();
  }

  /**
   * Selects a method corresponding to the action called from the client side.
   */
  private boolean _actionSwitch()
  {
    switch(action) {
      case "insertBulk":
        _insertBulk();
        return true;
      case "clear":
        _clear();
        return true;
      default:
        Log.d(LOG_TAG, "Invalid action: " + action + " passed");
        callbackContext.sendPluginResult(new PluginResult(Status.INVALID_ACTION, "action-invalid"));
        return false;
    }
  }

  /**
   * Performs multiple insertions of entries into the call log.
   */
  private void _insertBulk()
  {
    cordova.getThreadPool().execute(new Runnable()
    {
      public void run()
      {
        PluginResult cordovaResult;
        int iInserted = 0;

        try
        {
          JSONArray logList = args.optJSONArray(1);
          if(logList != null)
          {
            ContentValues[] valueList = new ContentValues[logList.length()];

            for (int i = 0 ; i < logList.length(); i++)
            {
              JSONObject logItem = logList.optJSONObject(i);
              if(logItem != null)
              {
                ContentValues value = new ContentValues();

                value.put(CallLog.Calls.NUMBER, logItem.getString(CallLog.Calls.NUMBER));
                value.put(CallLog.Calls.DATE, logItem.getLong(CallLog.Calls.DATE));
                value.put(CallLog.Calls.DURATION, logItem.getLong(CallLog.Calls.DURATION));
                value.put(CallLog.Calls.TYPE, logItem.getInt(CallLog.Calls.TYPE));
                value.put(CallLog.Calls.NEW, logItem.getInt(CallLog.Calls.NEW));
                value.put(CallLog.Calls.IS_READ, logItem.getInt(CallLog.Calls.IS_READ));

                valueList[i] = value;
              }
            }

            iInserted = CallLogWrite.this.cordova.getActivity()
              .getContentResolver()
              .bulkInsert(CallLog.Calls.CONTENT_URI, valueList);
          }

          Log.d(LOG_TAG, "Inserted: " + iInserted);
          cordovaResult = new PluginResult(Status.OK, iInserted);
        }
        catch (JSONException e)
        {
          Log.d(LOG_TAG, "Got JSON Exception " + e.getMessage());
          cordovaResult = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
        }
        catch (Exception e)
        {
          Log.d(LOG_TAG, "Got Exception " + e.getMessage());
          cordovaResult = new PluginResult(Status.ERROR, e.getMessage());
        }

        callbackContext.sendPluginResult(cordovaResult);
      }
    });
  }

  /**
   * Clears the call log.
   */
  private void _clear()
  {
    cordova.getThreadPool().execute(new Runnable()
    {
      public void run()
      {
        PluginResult cordovaResult;

        try
        {
          int iDeleted = CallLogWrite.this.cordova.getActivity()
            .getContentResolver()
            .delete(CallLog.Calls.CONTENT_URI, null, null);

          Log.d(LOG_TAG, "Deleted: " + iDeleted);
          cordovaResult = new PluginResult(Status.OK, iDeleted);
        }
        catch (Exception e)
        {
          Log.d(LOG_TAG, "Got Exception " + e.getMessage());
          cordovaResult = new PluginResult(Status.ERROR, e.getMessage());
        }

        callbackContext.sendPluginResult(cordovaResult);
      }
    });
  }
}