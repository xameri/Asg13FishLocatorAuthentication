package css.cis3334.fishlocatorfirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
// I have already commited this project almost two weeks ago, but
// i am trying it again today and i do not know what went wrognt.

public class MainActivity extends AppCompatActivity {

    Button buttonAdd, buttonDetails, buttonDelete;          // two button widgets
    ListView listViewFish;                                  // listview to display all the fish in the database
    ArrayAdapter<Fish> fishAdapter;
    List<Fish> fishList;
    FishFirebaseData fishDataSource;
    DatabaseReference myFishDbRef;
    int positionSelected;
    Fish fishSelected;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkUserAuthenticated();
        setupFirebaseDataChange();
        setupListView();
        setupAddButton();
        setupDetailButton();
        setupDeleteButton();
    }

    private void checkUserAuthenticated() {
        mAuth = FirebaseAuth.getInstance(); //declare object for Firebase
        mAuthListener = new FirebaseAuth.AuthStateListener() { //initialized mAuthListener
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //track the user when they sign in or out using the firebaseAuth
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    Log.d("CSS3334","onAuthStateChanged - User NOT is signed in");
                    Intent signInIntent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(signInIntent);
                }
            }
        };
    }

    private void setupFirebaseDataChange() {
        fishDataSource = new FishFirebaseData();
        myFishDbRef = fishDataSource.open(this);
        myFishDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("CIS3334", "Starting onDataChange()");        // debugging log
                fishList = fishDataSource.getAllFish(dataSnapshot);
                // Instantiate a custom adapter for displaying each fish
                fishAdapter = new FishAdapter(MainActivity.this, android.R.layout.simple_list_item_single_choice, fishList);
                // Apply the adapter to the list
                listViewFish.setAdapter(fishAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("CIS3334", "onCancelled: ");
            }
        });
    }

    private void setupListView() {
        listViewFish = (ListView) findViewById(R.id.ListViewFish);
        listViewFish.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View parent,
                                    int position, long id) {
                positionSelected = position;
                Log.d("MAIN", "Fish selected at position " + positionSelected);
            }
        });
    }

    private void setupAddButton() {
        // Set up the button to add a new fish using a seperate activity
        buttonAdd = (Button) findViewById(R.id.buttonAddFish);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Start up the add fish activity with an intent
                Intent detailActIntent = new Intent(view.getContext(), AddFishActivity.class);
                finish();
                startActivity(detailActIntent);
            }
        });
    }

    private void setupDetailButton() {
        // Set up the button to display details on one fish using a seperate activity
        buttonDetails = (Button) findViewById(R.id.buttonDetails);
        buttonDetails.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("MAIN", "onClick for Details");
                Intent detailActIntent = new Intent(view.getContext(), DetailActivity.class);
                detailActIntent.putExtra("Fish", fishList.get(positionSelected));
                finish();
                startActivity(detailActIntent);
            }
        });
    }

    private void setupDeleteButton() {
        // Set up the button to display details on one fish using a seperate activity
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("MAIN", "onClick for Delete");
                Log.d("MAIN", "Delete at position " + positionSelected);
                fishDataSource.deleteFish(fishList.get(positionSelected));
                fishAdapter.remove( fishList.get(positionSelected) );
                fishAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStart() {
        //initiate the authentication listener
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener); // update the listener on the users place
    }

    @Override
    public void onStop() {
        //discontinue the authentication
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener); // remove the listener
        }
    }
}
