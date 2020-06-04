package com.mad.p03.np2020.routine.Class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.FormatException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.p03.np2020.routine.database.FocusDBHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * Model used to manage the user data
 *
 * @author Lee Quan Sheng and Jeyavishnu
 * @since 04-06-2020
 */
public class User implements Parcelable {

    /**The table name for this model*/
    public static final String TABLE_NAME = "user"; //Name of the table

    /**Used as the primary key for this table*/
    public static final String COLUMN_NAME_ID = "id";
    /**Column name for table,  to identify the name of the user*/
    public static final String COLUMN_NAME_NAME = "name";
    /**Column name for table,  to identify the email of the user*/
    public static final String COLUMN_NAME_EMAIL = "email";
    /**Column name for table,  to identify the password of the task*/
    public static final String COLUMN_NAME_PASSWORD = "password"; //The password will be encrypted

    /**
     * The query needed to create a sql database
     * for the user
     */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME_NAME + " TEXT," +
                    COLUMN_NAME_EMAIL + " TEXT," +
                    COLUMN_NAME_PASSWORD + " TEXT)";

    /**
     * The query needed to drop the table for task
     */
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    final private String TAG = "User";

    //Member variable
    private FirebaseUser mAuth;
    private String mName;
    private String mUID;
    private String mEmailAddr;
    private String mPassword;
    private Date mDateOfBirth;
    private List<Section> mSectionList = new ArrayList<>();
    private String mPPID;
    private List<Label> mListLabel = new ArrayList<>();
    private ArrayList<Focus> mFocusList = new ArrayList<>();
    private DatabaseReference myRef;
    private FocusDBHelper focusDBHelper;

    /**
     * Parcelable constructor for custom object
     *
     * @param in Container for a message (data and object references) that is sent through an IBinder
     */
    protected User(Parcel in) {
        mAuth = in.readParcelable(FirebaseUser.class.getClassLoader());
        mName = in.readString();
        mUID = in.readString();
        mEmailAddr = in.readString();
        mPassword = in.readString();
        mPPID = in.readString();
        mFocusList = in.createTypedArrayList(Focus.CREATOR);
    }

    /**
     * Implementation of parcelable interface of a type implements the Parcelable.Creator Interface
     *
     * @param in Container for a message (data and object references) that is sent through an IBinder
     */
    public static final Creator<User> CREATOR = new Creator<User>() {

        /**
         * Implementation of parcelable interface of a type implements the Parcelable.Creator Interface
         *
         * @param in Container for a message (data and object references) that is sent through an IBinder
         * @return User return object create from parcel
         */
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        /**
         * Implementation of parcelable interface of a type implements the Parcelable.Creator Interface
         *
         * @param size Container for a message (data and object references) that is sent through an IBinder
         * @return customObject Array size
         */
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * Method to get all FocusList for current User
     *
     * @return ArrayList of focus object
     */
    public ArrayList<Focus> getmFocusList() {
        return mFocusList;
    }

    /**
     * Method to set all FocusList for current User
     *
     * @return ArrayList of focus object
     */
    public void setmFocusList(ArrayList<Focus> mFocusList) {
        this.mFocusList = mFocusList;
    }

    /**
     * Method to add focus object to Focus List
     *
     * @param focus Set object focus to the focus
     */
    public void addFocusList(Focus focus) {
        this.mFocusList.add(focus);
    }

    /**
     * Method to clear Focus List
     *
     */
    public void clearFocusList() {
        setmFocusList(new ArrayList<Focus>());
    }

    /**
     * Read the firebase of User
     *
     * @param context set context to the current content
     */
    public void readFocusFirebase(Context context) {
        myRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUID());
        focusDBHelper = new FocusDBHelper(context);

        //Clear all data since there is a change to the database so it can be updated

        myRef.child("FocusData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                focusDBHelper.deleteAll();
                clearFocusList();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Focus focus = new Focus();
                    focus.setFbID((String) singleSnapshot.child("fbID").getValue());
                    focus.setmCompletion((String) singleSnapshot.child("mCompletion").getValue());
                    focus.setmDateTime((String) singleSnapshot.child("mDateTime").getValue());
                    focus.setmDuration((String) singleSnapshot.child("mDuration").getValue());
                    focus.setmTask((String) singleSnapshot.child("mTask").getValue());
                    addFocusList(focus);
                    focusDBHelper.addData(focus);
                }
                setmFocusList(focusDBHelper.getAllData());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public User() {
    }

    /**
     *
     * This will set the name and the password
     * to the object
     *
     * @param name The name of the user
     * @param password The password of the user
     */
    User(String name, String password) {
        this.mName = name;
        this.mPassword = password;
    }

    /**
     *
     * This will set the UID, name, password,
     * dob and email into the object
     *
     * @param UID The unique ID of the user
     * @param name The name of the user
     * @param password The password of the user
     * @param dob The date of birth the user
     * @param Email The email of the user
     */
    public User(String UID, String name, String password, Date dob, String Email) {
        this.mUID = UID;
        this.mName = name;
        this.mPassword = password;
        this.mDateOfBirth = dob;
        this.mEmailAddr = Email;
    }

    /**
     *
     * You can set the name, password and the email of the
     * user in to the object
     *
     * @param name The name of the user
     * @param password The password of the user
     * @param emailAddr The email of the user
     */
    public User(String name, String password, String emailAddr) {
        this.mName = name;
        this.mPassword = password;
        this.mEmailAddr = emailAddr;
    }

    /**
     *
     * This methods is to check if the name is empty and
     * set it into the object
     *
     * @param name This parameter take in the name of the user
     *             and set it
     * @throws FormatException On input given if it empty of not
     */
    public void setName(String name) throws FormatException {

        if (!name.isEmpty()) {
            mName = name;
        } else
            throw new FormatException("Text is empty");
    }

    /**
     *
     * This method is to check if the email is empty and if it's in the email format and
     * set it into the object
     *
     * @param emailAdd This parameter take in the email address of the user
     *                     and set it
     * @throws FormatException On input given must follow the email format (@ and .com)
     * and not empty
     */
    public void setEmailAdd(String emailAdd) throws FormatException {

        if (!emailAdd.isEmpty()) {
            //Declare Constants
            String EMAILPATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (emailAdd.trim().matches(EMAILPATTERN)) {
                mEmailAddr = emailAdd;

            } else {
                //Error message for email when .com and @ is not present
                throw new FormatException("Text does not have @ and .com");


            }
        } else {
            throw new FormatException("Text is empty");
        }
    }

    /**
     *
     * This method is to check if the password is empty and if it's in the strong password format and
     * set it into the object
     *
     * @param password This parameter take in the password of the user
     *                 and set it
     * @throws FormatException On input given must follow the strong password format have 8 characters,
     * with special symbol and alphanumeric, not empty and no white space
     */
    public void setPassword(String password) throws FormatException {
        //TODO: Encrypt the password

        if (!password.isEmpty()) {
            String STRONGPASSWORD = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
            if (password.trim().matches(STRONGPASSWORD)) {
                mPassword = password;
            } else {


                //a digit must occur at least once
                //a lower case letter must occur at least once
                //an upper case letter must occur at least once
                //a special character must occur at least once
                //    no whitespace allowed in the entire string
                //anything, at least eight places though

                //Error message for password when password doesn't
                //have digit, lower and upper case, special character and min 8 letter
                throw new FormatException("Text doesn't meet strong password requirement");
            }
        } else {
            //Error message for empty text
            throw new FormatException("Text is empty");
        }

    }

    /**
     *
     * This method is to check if the Date of Birth is empty and if it's in the right
     * format (DD/MM/YYYY) and set it into the object
     *
     * @param dateOfBirth This parameter take in the Date of birth of the user
     *                    and set it
     * @throws FormatException On input given must follow the Date format (DD/MM/YYYY)
     */
    public void setDateOfBirth(String dateOfBirth) throws FormatException {
        if (!dateOfBirth.isEmpty()) {
            String DOBPATTERN = "[0-9]+[0-9]+/+[0-9]+[0-9]+/[0-9]+[0-9][0-9]+[0-9]";
            if (dateOfBirth.trim().matches(DOBPATTERN)) {
                //Get the date from String
                mDateOfBirth = stringToDate(dateOfBirth);

            } else {

                //Error message for DOB when it does match DD/MM/YYYY
                throw new FormatException("Text doesn't meet DOB (DD/MM/YYYY) requirement");
            }
        } else {
            //Error message for empty text
            throw new FormatException("Text is empty");
        }

    }

    /**
     * This method is used to set the
     * user unique ID into the object
     *
     * @param UID This parameter is used to set the UID
     *            of the user
     */
    public void setUID(String UID) {
        mUID = UID;
    }

    /**
     *
     * This method is used to set the
     * user authentication into the object
     *
     * @param auth This parameter is used to set the authentication
     *             info of the user from firebase
     */
    public void setAuth(FirebaseUser auth) {
        mAuth = auth;

        //Set the UID for this user
        this.setUID(this.mAuth.getUid());
    }


    /**@return String This return the UID of the user*/
    public String getUID() {
        return mUID;
    }

    /**@return FirebaseUser This return the authentication for this user by firebase*/
    public FirebaseUser getAuth() {
        return mAuth;
    }

    /**@return String This return the name of the user*/
    public String getName() {
        return mName;
    }

    /**@return String This return the email address the user*/
    public String getEmailAdd() {
        return mEmailAddr;
    }

    /**@return String This return the password of the user */
    public String getPassword() {
        return mPassword;
    }

    /**
     *
     * This method convert the date into string and
     * returns it
     *
     * @return String This return the date of birth of the user */
    public String getDateOfBirth() {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return mDateOfBirth == null ? null : dateFormat.format(mDateOfBirth);
    }


    /**
     * To convert string to date
     * The function has 2 possible returns
     * <p>
     * if Successfully changed a Date object will be return
     * else null will be returned
     *
     * @param DOB is a String that is provided by the user
     */

    private Date stringToDate(String DOB) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyyy");
        try {
            return sdf.parse(DOB);
        } catch (ParseException ex) {
            Log.e("Exception", "Date unable to change reason: " + ex.getLocalizedMessage());
            return null;
        }
    }


    /**
     *
     * Describe the kinds of special objects contained in this
     * Parcelable instance's marshaled representation.
     *
     * @return This return 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write to parcel
     *
     * @param dest set dest to current content
     * @param flags set flags to current content
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mAuth, flags);
        dest.writeString(mName);
        dest.writeString(mUID);
        dest.writeString(mEmailAddr);
        dest.writeString(mPassword);
        dest.writeString(mPPID);
        dest.writeTypedList(mFocusList);
    }
}