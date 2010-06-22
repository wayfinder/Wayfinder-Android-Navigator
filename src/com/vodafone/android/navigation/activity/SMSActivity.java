/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
package com.vodafone.android.navigation.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.telephony.gsm.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.vodafone.android.navigation.R;

public class SMSActivity extends Activity {

    public static final String KEY_PHONE_NUMBER = "key_phone_number";
    public static final String KEY_BODY = "key_body";

    private static final int REQUEST_CODE_PICK_CONTACT = 1;

    private EditText editPhonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setTitle(R.string.qtn_andr_send_sms_txt);
        this.setContentView(R.layout.sms);

        Intent intent = this.getIntent();
        String text = intent.getStringExtra(KEY_BODY);
        String phonenumber = intent.getStringExtra(KEY_PHONE_NUMBER);
        
        ImageButton btnPhonebook = (ImageButton) this.findViewById(R.id.btn_phonebook);
        btnPhonebook.setOnClickListener(new OnClickListener() {
            public void onClick(View button) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.People.CONTENT_URI), REQUEST_CODE_PICK_CONTACT);
            }
        });
        
        this.editPhonenumber = (EditText) this.findViewById(R.id.edit_phonenumber);
        if(phonenumber != null) {
            this.editPhonenumber.setText(phonenumber);
        }
        
        final EditText editText = (EditText) this.findViewById(R.id.edit_message);
        if(text != null) {
            editText.setText(text);
        }
        
        Button btnSend = (Button) this.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View button) {
                Context context = button.getContext();

                String phonenumber = editPhonenumber.getText().toString().trim();
                String message = editText.getText().toString().trim();
                if(phonenumber == null || phonenumber.length() == 0) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setTitle(R.string.qtn_andr_note_txt);
                    ab.setMessage(R.string.qtn_andr_u_enter_ph_no_txt);
                    ab.setNeutralButton(R.string.qtn_andr_ok_tk, null);
                    ab.create().show();
                }
                else if(message == null || message.length() == 0) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setTitle(R.string.qtn_andr_note_txt);
                    ab.setMessage(R.string.qtn_andr_u_enter_mess_txt);
                    ab.setNeutralButton(R.string.qtn_andr_ok_tk, null);
                    ab.create().show();
                }
                else {
                    Toast.makeText(context, R.string.qtn_andr_sending_mess_txt, Toast.LENGTH_SHORT).show();
                    try {
                        SmsManager.getDefault().sendTextMessage(phonenumber, null, message, null, null);
                        finish();
                    } catch(Exception e) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(context);
                        ab.setTitle(R.string.qtn_andr_note_txt);
                        ab.setMessage("Error when sending message. Is this a valid phonenumber?\n " + e);
                        ab.setNeutralButton(R.string.qtn_andr_ok_tk, null);
                        ab.create().show();
                    }
                }
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == REQUEST_CODE_PICK_CONTACT) {
            if(resultCode == Activity.RESULT_OK) {
                Uri contactUri = intent.getData();
                Cursor cursor = managedQuery(contactUri, new String[] { People.NUMBER }, null, null, null);
                if(cursor != null) {
                    cursor.moveToNext();
                    String phonenumber = cursor.getString(0);
                    editPhonenumber.setText(phonenumber);
                }
            }
            else {
                System.out.println("SMSActivity.onActivityResult() ResultCode not ok: " + resultCode);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }
}
