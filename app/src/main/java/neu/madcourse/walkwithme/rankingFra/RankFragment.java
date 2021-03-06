package neu.madcourse.walkwithme.rankingFra;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import neu.madcourse.walkwithme.R;
import neu.madcourse.walkwithme.ranking.ItemRank;
import neu.madcourse.walkwithme.ranking.RankAdapter;
import neu.madcourse.walkwithme.userlog.LoginActivity;

public class RankFragment extends Fragment{
    private List<ItemRank> itemRankList;
    private RecyclerView recyclerView;
    private RankAdapter rankAdapter;
    private DatabaseReference databaseReference;
    private String LOG = "RANKING_ACTIVITY";
    private TextView tvCurrentUser;
    private SharedPreferences sharedPreferences;
    private TextView etDateOfToday;
    List<String> usernames;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("rank", Context.MODE_PRIVATE);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {

        etDateOfToday = view.findViewById(R.id.etToday);
        String dateOfToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        String title = dateOfToday + "  You ranked here";
        etDateOfToday.setText(title);
        tvCurrentUser = view.findViewById(R.id.tvUserName);
        tvCurrentUser.setText(LoginActivity.currentUser);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemRankList = new ArrayList<>();
        usernames = new ArrayList<>();


        // fetch data from firebase
        //databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference = FirebaseDatabase.getInstance().getReference("Rankings");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // ItemRank itemRank = ds.child("username");
                    String username = ds.child("username").getValue(String.class);
                    // Log.d(LOG, String.valueOf(ds.child("Step Count").getValue(Long.class)));
                    int steps = Integer.parseInt(String.valueOf(ds.child("steps").getValue(Long.class)));
                    int likesReceived = Integer.parseInt(String.valueOf(ds.child("likesReceived").getValue(Long.class)));
                    ItemRank itemRank = new ItemRank(username, steps, likesReceived);
                    itemRankList.add(itemRank);
                }
                processItemRankList(view, itemRankList);
                // need to filter out the current user
                // pass the fetched data to adapter
                rankAdapter = new RankAdapter(itemRankList);
                recyclerView.setAdapter(rankAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        final FloatingActionButton search = view.findViewById(R.id.addNewFriend);
        Log.d("Search Friends", "about to click");
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Search Friends", "onClick: click add friend");
                startSearchDialog();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rank, container, false);
    }


    private void processItemRankList(@NonNull final View view, List<ItemRank> itemRankList) {
        sortBySteps(itemRankList);
        addRankIdToEachItem(view, itemRankList);
    }

    private void addRankIdToEachItem(@NonNull final View view, List<ItemRank> itemRankList) {
        int indexOfCurrentUser = -1;
        for (int i = 0; i < itemRankList.size(); i++) {
            ItemRank currentItem = itemRankList.get(i);
            currentItem.setRankId(i + 1);
            if (currentItem.getUsername().equals(LoginActivity.currentUser)) {
                indexOfCurrentUser = i;
                setCurrentUserStatus(view, currentItem);
            }
        }
        itemRankList.remove(indexOfCurrentUser);
    }

    private void setCurrentUserStatus(@NonNull final View view, ItemRank currentItem) {
        TextView tvRank = view.findViewById(R.id.tvRank);
        tvRank.setText(String.valueOf(currentItem.getRankId()));
        TextView tvSteps = view.findViewById(R.id.tvSteps);
        tvSteps.setText(String.valueOf(currentItem.getSteps()));
        TextView tvLikes = view.findViewById(R.id.tvLikes);
        tvLikes.setText(String.valueOf(currentItem.getLikesReceived()));
    }

    private void sortBySteps(List<ItemRank> itemRankList) {
        Collections.sort(itemRankList, (itemOne, itemTwo) -> itemTwo.getSteps() - itemOne.getSteps());
    }

    public void startSearchDialog(){
        final Dialog d = new Dialog(getActivity());
        d.setTitle("SearchFriends");
        d.setContentView(R.layout.dailog_search_friend);
        Button exist = (Button) d.findViewById(R.id.exist);
        Button add = (Button) d.findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //reset goal
                Log.d("Search Friend", "add button is clicked");
                d.dismiss();
            }
        });
        exist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("Search Friend", "exist button is clicked");
                d.dismiss();
            }
        });
        d.show();

    }
}