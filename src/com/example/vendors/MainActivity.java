package com.example.vendors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	SQLiteDatabase db;
	DbHelper mydb = new DbHelper(MainActivity.this);
	private SimpleCursorAdapter dataAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = mydb.getWritableDatabase();
		new GetData()
				.execute("https://testapiv2.herokuapp.com/api/test/vendors");

	}

	private void displayListView() {

		Cursor cursor = DbHelper.retrieveAllEntries(mydb.getReadableDatabase());
		// The desired columns to be bound
		String[] columns = new String[] { "name", "category", "locality" };
		int[] to = new int[] { R.id.name, R.id.category, R.id.locality };
		dataAdapter = new SimpleCursorAdapter(this, R.layout.list_row, cursor,
				columns, to, 0);

		ListView listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(dataAdapter);
		
		TextView heading = (TextView) findViewById(R.id.heading);
		heading.setVisibility(View.VISIBLE);
		
		EditText myFilter = (EditText) findViewById(R.id.myFilter);
		myFilter.setVisibility(View.VISIBLE);
		myFilter.addTextChangedListener(new TextWatcher() {
		 
		   public void afterTextChanged(Editable s) {
		   }
		 
		   public void beforeTextChanged(CharSequence s, int start, 
		     int count, int after) {
		   }
		 
		   public void onTextChanged(CharSequence s, int start, 
		     int before, int count) {
		    dataAdapter.getFilter().filter(s.toString());
		   }
		  });
		   
		  dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {
		         public Cursor runQuery(CharSequence constraint) {
		             return DbHelper.retrieveEntriesByName(mydb.getReadableDatabase(), constraint.toString());
		         }
		     });
		 
		 }

	class GetData extends AsyncTask<String, String, String> {
		ProgressDialog pd;

		@Override
		protected void onPostExecute(String result) {

			JSONArray vendorArray = null;
			try {
				vendorArray = new JSONArray(result);
				for (int i = 0; i < vendorArray.length(); i++) {
					JSONObject vendor = vendorArray.getJSONObject(i);
					DbHelper.addRowInVendortable(db, vendor.getString("name"),
							vendor.getString("locality"),
							vendor.getString("category"));

				}
				pd.dismiss();
				displayListView();

			} catch (JSONException e) {
				e.printStackTrace();
			}

			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(MainActivity.this);
			pd.setMessage("Loading to database...");
			pd.setCancelable(false);
			pd.show();

		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet method = new HttpGet(params[0]);
				HttpResponse response = httpclient.execute(method);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					return "No string.";
				}
			} catch (Exception e) {
				return "Network problem";
			}

		}
	}

}
