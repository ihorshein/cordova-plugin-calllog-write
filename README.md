# cordova-plugin-calllog-write
Cordova plugin that allows an Android application to write the user's call log data.

## platform
- Android

## installation
```
cordova plugin add https://github.com/ihorshein/cordova-plugin-calllog-write
```

## permission
The plugin requires the following permission: [WRITE_CALL_LOG](https://developer.android.com/reference/android/Manifest.permission#WRITE_CALL_LOG)

By default, permissions can be requested on every call, but this behavior can be changed 
as will be shown in the example below.

## interface
```javascript
/**
 * Write multiple call logs to the device.
 *
 * @param {{
 *  number: string,
 *  date: number,
 *  duration: number,
 *  type: number,
 *  new: number,
 *  is_read: number
 * }[]} a_log An array of call log objects. Each object contains the keys from the following list:
 * * {@link CallLogWrite.NUMBER},
 * * {@link CallLogWrite.DATE},
 * * {@link CallLogWrite.DURATION},
 * * {@link CallLogWrite.TYPE},
 * * {@link CallLogWrite.NEW},
 * * {@link CallLogWrite.IS_READ}.
 * @param {boolean} has_permission Whether the application has permission or not.
 *  `false` if the permission should be requested, `true` otherwise.
 * @param {Function} call_success A success callback function.
 * @param {Function} call_error An error callback function.
 */
CallLogWrite::insertBulk(a_log, has_permission, call_success, call_error);
```
```javascript
/**
 * Clears the call log.
 *
 * @param {boolean} has_permission Whether the application has permission or not.
 *  `false` if the permission should be requested, `true` otherwise.
 * @param {Function} call_success A success callback function.
 * @param {Function} call_error An error callback function.
 */
CallLogWrite::clear(has_permission, call_success, call_error)
```


## field descriptions
```javascript
/**
 * The phone number as the user entered it.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#NUMBER
 */
NUMBER = 'number';

/**
 * The date the call occured, in milliseconds since the epoch.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#DATE
 */
DATE = 'date';

/**
 * The duration of the call in seconds.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#DURATION
 */
DURATION = 'duration';

/**
 * The type of the call (incoming, outgoing or missed).
 *
 * Allowed values:
 * * INCOMING_TYPE,
 * * OUTGOING_TYPE,
 * * MISSED_TYPE.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#TYPE
 */
TYPE = 'type';

/**
 * Whether or not the call has been acknowledged.
 * `1` if the call is new, `0` otherwise.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#NEW
 */
NEW = 'new';

/**
 * Whether this item has been read or otherwise consumed by the user.
 *
 * Unlike the {@link CallLogWrite.NEW} field, which requires the user to have acknowledged the existence of the entry,
 *  this implies the user has interacted with the entry.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#IS_READ
 */
IS_READ = 'is_read';

/**
 * Call log type for incoming calls.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#INCOMING_TYPE
 */
INCOMING_TYPE = 1;

/**
 * Call log type for outgoing calls.
 *
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#OUTGOING_TYPE
 */
OUTGOING_TYPE = 2;

/**
 * Call log type for missed calls.
 * 
 * @link https://developer.android.com/reference/android/provider/CallLog.Calls#MISSED_TYPE
 */
MISSED_TYPE = 3;
```


## example
```javascript
try
{
  const o_call_log_write = cordova.plugins.CallLogWrite;

  // Clearing the call list.
  o_call_log_write.clear(false,
    function()
    {
      alert('Delete ' + JSON.stringify(arguments));

      // Write multiple call logs.
      const o_log1 = {};
      o_log1[o_call_log_write.NUMBER] = '1234-5678-90';
      o_log1[o_call_log_write.DATE] = Date.now();
      o_log1[o_call_log_write.DURATION] = 60;
      o_log1[o_call_log_write.TYPE] = o_call_log_write.OUTGOING_TYPE;
      o_log1[o_call_log_write.NEW] = 0;
      o_log1[o_call_log_write.IS_READ] = 0;

      const o_log2 = {};
      o_log2[o_call_log_write.NUMBER] = '4552-1234-90';
      o_log2[o_call_log_write.DATE] = Date.now();
      o_log2[o_call_log_write.DURATION] = 300;
      o_log2[o_call_log_write.TYPE] = o_call_log_write.MISSED_TYPE;
      o_log2[o_call_log_write.NEW] = 1;
      o_log2[o_call_log_write.IS_READ] = 0;

      o_call_log_write.insertBulk(
        [o_log1, o_log2],
        true, // Application already has access to the permission.
        function()
        {
          alert('Insert ' + JSON.stringify(arguments));
        },
        function()
        {
          alert('Error ' + JSON.stringify(arguments));
        }
      );
    },
    function()
    {
      alert('Error ' + JSON.stringify(arguments));
    }
  );
}
catch(e)
{
  alert(e);
}
```