package com.ihorshein.cordova.plugins;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import android.content.pm.PackageManager;
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

  private JSONArray args;
  private CallbackContext callbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
  {

    this.args = args;
    this.callbackContext = callbackContext;

    // Whether the application has permission or not (received from the client).
    Boolean hasPermissionEx = args.optBoolean(1);

    // The actual state of the permit (received from the system).
    Boolean hasPermissionIn = cordova.hasPermission(android.Manifest.permission.WRITE_CALL_LOG);

    if("writeBulk".equals(action))
    {
      if(hasPermissionEx || hasPermissionIn)
      {
        if(!hasPermissionIn)
        {
          Log.d(LOG_TAG, "Permission denied");
          callbackContext.sendPluginResult(new PluginResult(Status.ERROR, "permission-access"));

          return false;
        }

        writeBulk();
      }
      else
      {
        cordova.requestPermission(this, WRITE_CALL_LOG_REQ_CODE, android.Manifest.permission.WRITE_CALL_LOG);
      }

      return true;
    }

    Log.d(LOG_TAG, "Invalid action: " + action + " passed");
    callbackContext.sendPluginResult(new PluginResult(Status.INVALID_ACTION, "action-invalid"));

    return false;
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
      writeBulk();
  }

  private void writeBulk()
  {
    cordova.getThreadPool().execute(new Runnable()
    {
      public void run()
      {
        PluginResult result;
        int iInserted = 0;

        try
        {
          JSONArray logList = args.optJSONArray(0);
          if(logList != null)
          {
            for (int i = 0 ; i < logList.length(); i++)
            {
              JSONObject logItem = logList.optJSONObject(i);
              if(logItem != null)
              {
                ContentValues values = new ContentValues();

                values.put(android.provider.CallLog.Calls.NUMBER, logItem.getString(android.provider.CallLog.Calls.NUMBER));
                values.put(android.provider.CallLog.Calls.DATE, logItem.getLong(android.provider.CallLog.Calls.DATE));
                values.put(android.provider.CallLog.Calls.DURATION, logItem.getLong(android.provider.CallLog.Calls.DURATION));
                values.put(android.provider.CallLog.Calls.TYPE, logItem.getInt(android.provider.CallLog.Calls.TYPE));
                values.put(android.provider.CallLog.Calls.NEW, logItem.getInt(android.provider.CallLog.Calls.NEW));
                values.put(android.provider.CallLog.Calls.IS_READ, logItem.getInt(android.provider.CallLog.Calls.IS_READ));

                Uri uri = CallLogWrite.this.cordova.getActivity()
                  .getContentResolver()
                  .insert(android.provider.CallLog.Calls.CONTENT_URI, values);

                iInserted++;
              }
            }
          }

          Log.d(LOG_TAG, "Inserted: " + iInserted);
          result = new PluginResult(Status.OK, iInserted);
        }
        catch (JSONException e)
        {
          Log.d(LOG_TAG, "Got JSON Exception " + e.getMessage());
          result = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
        }
        catch (Exception e)
        {
          Log.d(LOG_TAG, "Got Exception " + e.getMessage());
          result = new PluginResult(Status.ERROR, e.getMessage());
        }

        callbackContext.sendPluginResult(result);
      }
    });
  }
}