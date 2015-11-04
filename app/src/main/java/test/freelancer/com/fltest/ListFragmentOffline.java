package test.freelancer.com.fltest;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import test.freelancer.com.fltest.rest.ServiceGenerator;
import test.freelancer.com.fltest.rest.TvGuideInterface;
import test.freelancer.com.fltest.rest.model.Program;
import test.freelancer.com.fltest.rest.model.ProgramCount;
import test.freelancer.com.fltest.rest.model.ProgramList;

/**
 * List that displays the TV Programmes
 */
public class ListFragmentOffline extends Fragment {

    RecyclerView recyclerView;
    RVAdapter adapter;
    List<Program> recyclerViewPrograms = new ArrayList<Program>();
    List<Program> programs = new ArrayList<Program>();
    List<Program> programList = new ArrayList<Program>();
    Integer count = 0;
    int pageCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recyvlerview, container, false);
        return recyclerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RVAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page) {
                refreshList(page);
            }
        });

        if(!checkOfflineStorage()) {
            getPrograms(0);
        }
        getProgramsFromStorage();
        refreshList(0);
    }

    private boolean checkOfflineStorage() {
        boolean isOfflineDataAvailable = false;

        List<Program> progs = Program.listAll(Program.class);
        List<ProgramCount> counts = ProgramCount.listAll(ProgramCount.class);
        if( (progs != null && progs.size() > 0) && (counts != null && counts.size() > 0)) {
            isOfflineDataAvailable = true;
        }

        return isOfflineDataAvailable;
    }

    private void getProgramsFromStorage() {
        List<Program> progs = Program.listAll(Program.class);
        if(progs != null && progs.size() > 0) {
            programs = progs;
        }
        List<ProgramCount> counts = ProgramCount.listAll(ProgramCount.class);
        if(counts != null && counts.size() > 0) {
            count = counts.get(0).count;
        }
    }

    private void saveToStorage() {
        Program.deleteAll(Program.class);
        ProgramCount.deleteAll(ProgramCount.class);

        for(int i = 0; i < programList.size(); i++) {
            Program program = programList.get(i);
            Program newProgram = new Program();
            newProgram.name = program.name;
            newProgram.startTime = program.startTime;
            newProgram.endTime = program.endTime;
            newProgram.channel = program.channel;
            newProgram.rating = program.rating;
            newProgram.save();
        }

        ProgramCount programCount = new ProgramCount();
        programCount.count = count;
        programCount.save();
        getProgramsFromStorage();
        refreshList(0);
    }

    private void refreshList(int page) {

        if(programs.size() == 0) {
            return;
        }

        int size;

        if(((page + 1) * 10) < count) {
            size = 10;
        } else {
            size = count - ((page) * 10);
        }

        int startIndex = page * 10;

        for(int i = startIndex; i < startIndex + size; i++) {
            recyclerViewPrograms.add(programs.get(i));
        }

        adapter.notifyDataSetChanged();
    }


    private void getPrograms(int page) {

//        if(page != 0  && programs.size() >= count) {
//            return;
//        }

        TvGuideInterface tvGuide = ServiceGenerator.createService(TvGuideInterface.class);
        int start = page * 10;
        String pageStr =  String.valueOf(start);
        Call<ProgramList> call = tvGuide.getPrograms(pageStr);
        call.enqueue(new Callback<ProgramList>() {
            @Override
            public void onResponse(Response<ProgramList> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Program> progs = response.body().programList;
                    count = response.body().count;
//                    adapter.notifyDataSetChanged();
                    boolean limit = false;
                    if(programList.size() + progs.size() >= count) {
                        int size = count - programList.size();
                        for(int i = 0; i < size; i++) {
                            programList.add(progs.get(i));
                        }
                        limit = true;
                    } else {
                        programList.addAll(progs);
                    }

                    if(!limit) {
                        pageCount++;
                        getPrograms(pageCount);
                    } else {
                        saveToStorage();
                    }

                }

            }

            @Override
            public void onFailure(Throwable t) {
            }
        });

    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.layout_program_item, viewGroup, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Program program = recyclerViewPrograms.get(position);

            holder.name.setText(program.name);
            holder.start.setText(program.startTime);
            holder.end.setText(program.endTime);
            holder.channel.setText(program.channel);
            holder.rating.setText(program.rating);
        }

        @Override
        public int getItemCount() {
            return recyclerViewPrograms.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView start;
            TextView end;
            TextView channel;
            TextView rating;

            ViewHolder(View itemView) {
                super(itemView);
                name = (TextView)itemView.findViewById(R.id.textName);
                start = (TextView)itemView.findViewById(R.id.textStart);
                end = (TextView)itemView.findViewById(R.id.textEnd);
                channel = (TextView)itemView.findViewById(R.id.textChannel);
                rating = (TextView)itemView.findViewById(R.id.textRating);
            }
        }

    }

    public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
        public String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

        private int previousTotal = 0; // The total number of items in the dataset after the last load
        private boolean loading = true; // True if we are still waiting for the last set of data to load.
        private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
        int firstVisibleItem, visibleItemCount, totalItemCount;

        private int current_page = 0;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
            this.mLinearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                current_page++;

                onLoadMore(current_page);

                loading = true;
            }
        }

        public abstract void onLoadMore(int current_page);
    }


}
