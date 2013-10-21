package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Type;

import android.net.Uri;
import android.os.Bundle;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountsOverviewActivity extends ListActivity {

	private static final int DELETE_ID = 10;
	private static final int EDIT_ID = 20;

	// private SimpleCursorAdapter adapter;
	private AccountsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		this.getListView().setDividerHeight(2);
		fillData();
		this.registerForContextMenu(getListView());

		// control for default types
		Type.controlDefault(this.getContentResolver());
	}

	private void fillData() {
		ArrayList<Account> accounts = Account
				.getListAccounts(getContentResolver());

		if (accounts != null) {
			adapter = new AccountsAdapter(this, accounts);
			this.setListAdapter(adapter);
		} else if (adapter != null)
			adapter.clear();
	}

	@Override
	protected void onResume() {
		fillData();
		super.onResume();
	}

	private void createAccount() {
		Intent intent = new Intent(this, EditAccountActivity.class);
		startActivity(intent);
	}

	private void manageTypes() {
		Intent intent = new Intent(this, ManageTypesActivity.class);
		startActivity(intent);
	}

	// ADD and TYPES BUTTON ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			createAccount();
			return true;
		case R.id.menu_types:
			manageTypes();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// DELETE and EDIT BUTTONs -----------

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.account_overview_edit);
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE,
				R.string.account_overview_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Account account = new Account();

		// get id
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		account.setUri(Uri.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS
				+ "/" + info.id));

		// delete or edit
		switch (item.getItemId()) {
		case DELETE_ID:
			account.delete(getContentResolver());
			fillData();
			return true;
		case EDIT_ID:
			Intent intent = new Intent(this, EditAccountActivity.class);
			intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
					account.getUri());
			startActivity(intent);
		}

		return super.onContextItemSelected(item);
	}
	

	// ACCOUNT DETAILS -------------------

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, AccountDetailActivity.class);
		Uri accountUri = Uri
				.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS + "/"
						+ id);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE, accountUri);

		startActivity(intent);
	}
}
