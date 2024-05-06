const argscheck = require('cordova/argscheck');
const exec = require('cordova/exec');

/**
 * Cordova plugin that allows the application to write the user's call log data.
 *
 * @constructor
 */
function CallLogWrite()
{
  /**
   * The phone number as the user entered it.
   *
   * @constant
   * @type {string}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#NUMBER
   */
  this.NUMBER = 'number';

  /**
   * The date the call occured, in milliseconds since the epoch.
   *
   * @constant
   * @type {string}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#DATE
   */
  this.DATE = 'date';

  /**
   * The duration of the call in seconds.
   *
   * @constant
   * @type {string}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#DURATION
   */
  this.DURATION = 'duration';

  /**
   * The type of the call (incoming, outgoing or missed).
   *
   * Allowed values:
   * * {@link CallLogWrite.INCOMING_TYPE},
   * * {@link CallLogWrite.OUTGOING_TYPE},
   * * {@link CallLogWrite.MISSED_TYPE}.
   *
   * @constant
   * @type {string}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#TYPE
   */
  this.TYPE = 'type';

  /**
   * Whether or not the call has been acknowledged.
   * `1` if the call is new, `0` otherwise.
   *
   * @constant
   * @type {string}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#NEW
   */
  this.NEW = 'new';

  /**
   * Whether this item has been read or otherwise consumed by the user.
   *
   * Unlike the {@link CallLogWrite.NEW} field, which requires the user to have acknowledged the existence of the entry,
   *  this implies the user has interacted with the entry.
   *
   * @constant
   * @type {string}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#IS_READ
   */
  this.IS_READ = 'is_read';

  /**
   * Call log type for incoming calls.
   *
   * @constant
   * @type {number}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#INCOMING_TYPE
   */
  this.INCOMING_TYPE = 1;

  /**
   * Call log type for outgoing calls.
   *
   * @constant
   * @type {number}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#OUTGOING_TYPE
   */
  this.OUTGOING_TYPE = 2;

  /**
   * Call log type for missed calls.
   *
   * @constant
   * @type {number}
   *
   * @link https://developer.android.com/reference/android/provider/CallLog.Calls#MISSED_TYPE
   */
  this.MISSED_TYPE = 3;
}

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
CallLogWrite.prototype.writeBulk = function(a_log, has_permission, call_success, call_error)
{
  argscheck.checkArgs('a*ff', 'CallLogWrite.writeBulk', arguments);

  const a_key_allowed = [this.NUMBER, this.DATE, this.DURATION, this.TYPE, this.NEW, this.IS_READ];

  for(const i in a_log)
  {
    if(!a_log.hasOwnProperty(i))
      continue;

    argscheck.checkArgs('snnnnn', 'CallLogWrite.writeBulk', Object.values(a_log[i]));

    const a_key = Object.keys(a_log[i]);

    if(a_key.length !== a_key_allowed.length)
      throw new Error('CallLogWrite: The call log object does not have the correct number of fields.');

    for(const j in a_key_allowed)
    {
      if(a_key.indexOf(a_key_allowed[j]) === -1)
        throw new Error('CallLogWrite: The call log object does not have the correct fields.');
    }
  }

  exec(call_success, call_error, 'CallLogWrite', 'writeBulk', [a_log, has_permission]);
};

module.exports = new CallLogWrite();