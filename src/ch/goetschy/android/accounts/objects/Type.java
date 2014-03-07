package ch.goetschy.android.accounts.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.TransactionTable;
import ch.goetschy.android.accounts.database.TypeTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

public class Type implements Serializable, Savable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private long id;
	private String name;
	private Uri uri;
	private int order;
	private int color;

	public static final String[] DEFAULT_TYPES = new String[] { "other",
			"clothes", "food", "loan", "salary", "leisure" };

	public Type() {
		setId(0);
		setName(null);
		setUri(null);
		setColor(0);
		setOrder(0);
	}

	public Type(Cursor cursor) {
		setUri(null);
		if (cursor != null)
			loadFromCursor(cursor);
		else
			throw new NullPointerException("Null cursor");
	}

	public Type(long p_id, String p_name) {
		setId(p_id);
		setName(p_name);
		setUri(null);
		setColor(0);
		setOrder(0);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public static ArrayList<Type> getTypes(ContentResolver contentResolver) {
		ArrayList<Type> typesList = new ArrayList<Type>();

		Cursor cursor = contentResolver.query(
				MyAccountsContentProvider.CONTENT_URI_TYPES, null, null, null,
				null);

		if (BuildConfig.DEBUG)
			Log.w("type", "cursor movetofirst");
		if (cursor != null && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				if (BuildConfig.DEBUG)
					Log.w("type", "cursor line");
				typesList.add(new Type(cursor));
				cursor.moveToNext();
			}
			if (BuildConfig.DEBUG)
				Log.w("type", "cursor close");
			cursor.close();
		} else
			return null;

		return typesList;
	}

	public void saveInDB(ContentResolver contentResolver) {

		ContentValues values = new ContentValues();
		values.put(TypeTable.COLUMN_NAME, name);
		values.put(TypeTable.COLUMN_COLOR, color);
		values.put(TypeTable.COLUMN_ORDER, order);

		if (uri == null)
			contentResolver.insert(MyAccountsContentProvider.CONTENT_URI_TYPES,
					values);
		else
			contentResolver.update(uri, values, null, null);
	}

	public boolean loadFromDB(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(uri, null, null, null, null);

		if (cursor.moveToFirst()) {
			loadFromCursor(cursor);
			cursor.close();
			return true;
		}
		return false;
	}

	public void delete(ContentResolver contentResolver) {
		if (uri != null)
			contentResolver.delete(uri, null, null);
		// TODO
		// control if transaction is connected
	}

	public boolean loadNameAndColorFromDB(ContentResolver contentResolver) {
		String[] projection = new String[] { TypeTable.COLUMN_COLOR,
				TypeTable.COLUMN_NAME };
		Cursor cursor = contentResolver.query(
				MyAccountsContentProvider.CONTENT_URI_TYPES, projection,
				TypeTable.COLUMN_ID + "=" + id, null, null);
		if (cursor.moveToFirst()) {
			this.setColor(cursor.getInt(cursor
					.getColumnIndex(TypeTable.COLUMN_COLOR)));
			this.setName(cursor.getString(cursor
					.getColumnIndex(TypeTable.COLUMN_NAME)));
			cursor.close();
			return true;
		}
		return false;
	}

	private void loadFromCursor(Cursor cursor) {
		this.setId(cursor.getLong(cursor.getColumnIndex(TypeTable.COLUMN_ID)));
		this.setName(cursor.getString(cursor
				.getColumnIndex(TypeTable.COLUMN_NAME)));

		this.setOrder(cursor.getInt(cursor
				.getColumnIndex(TypeTable.COLUMN_ORDER)));
		this.setColor(cursor.getInt(cursor
				.getColumnIndex(TypeTable.COLUMN_COLOR)));
	}

	@Override
	public String toString() {
		return getName();
	}

	// function who controls if all the default types are there
	public static boolean controlDefault(ContentResolver contentResolver) {
		ArrayList<Type> typesList = getTypes(contentResolver);
		ArrayList<String> sTypesList = toStringArrayList(typesList);

		if (sTypesList.isEmpty())
			return false;
		else {
			for (String i : DEFAULT_TYPES) {
				if (BuildConfig.DEBUG)
					Log.w("type", "type : " + i);
				if (!sTypesList.contains(i))
					return false;
			}
			return true;
		}
	}

	// function who adds the default types
	public static void addDefault(ContentResolver contentResolver) {
		ArrayList<Type> typesList = getTypes(contentResolver);
		ArrayList<String> sTypesList = toStringArrayList(typesList);

		ArrayList<String> toAdd = new ArrayList<String>();

		if (sTypesList.isEmpty()) {
			// add all default types
			for (String i : DEFAULT_TYPES) {
				toAdd.add(i);
			}
		} else {
			// add only absent default types
			for (String i : DEFAULT_TYPES) {
				if (!sTypesList.contains(i))
					toAdd.add(i);
			}
		}

		// save all in DB
		for (String i : toAdd) {
			Type newType = new Type();
			newType.setName(i);
			newType.setColor(Color.WHITE);

			newType.saveInDB(contentResolver);
		}
	}

	// converts array to arraylist
	public static ArrayList<String> toStringArrayList(ArrayList<Type> types) {
		ArrayList<String> retour = new ArrayList<String>();

		if (types != null) {
			for (Type i : types)
				retour.add(i.getName());
		}

		return retour;
	}

	@Override
	public HashMap<String, String> getFields() {
		HashMap<String, String> fields = new HashMap<String, String>();

		fields.put("name", getName());
		fields.put("order", String.valueOf(getOrder()));
		fields.put("color", String.valueOf(getColor()));

		return fields;
	}
}
