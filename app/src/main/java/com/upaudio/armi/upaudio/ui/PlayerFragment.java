package com.upaudio.armi.upaudio.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.upaudio.armi.upaudio.R;
import com.upaudio.armi.upaudio.note.NotesDatabase;
import com.upaudio.armi.upaudio.note.UpAudioNote;
import com.upaudio.armi.upaudio.player.PodcastPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment implements ChildEventListener {

    @BindView(R.id.player_view)
    SimpleExoPlayerView playerView;

    @BindView(R.id.add_note)
    ImageButton addNoteButton;

    /**
     * Currently playing file
     */
    private String currentFileName;

    /**
     * Boolean tracking if player is recording a new UpAudio
     */
    private boolean isRecording = false;

    /**
     * Start time of current recording
     */
    private long startTime;

    /**
     * Podcast Player reference
     */
    private PodcastPlayer podcastPlayer;

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        podcastPlayer = PodcastPlayer.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String fileToPlay = getArguments().getString(MainActivity.EXTRA_FILE_NAME, "");
            updateFile(fileToPlay);
        }

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    isRecording = true;
                    startTime = podcastPlayer.getCurrentPosition();
                } else {
                    isRecording = false;
                    UpAudioNote upAudioNote = new UpAudioNote(currentFileName, startTime, podcastPlayer.getCurrentPosition());
                    upAudioNote.setNoteName("new note");
                    final String key = NotesDatabase.getInstance().saveUpAudio(upAudioNote);
                    Snackbar snackbar = Snackbar.make(view, R.string.note_added_message, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.edit_note, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(NotesActivity.ACTION_EDIT_NOTE);
                            intent.putExtra(NotesActivity.EXTRA_FILE, currentFileName);
                            intent.putExtra(NotesActivity.EXTRA_NOTE_ID, key);
                            startActivity(intent);
                        }
                    });
                    snackbar.show();
                }
            }
        });
        return view;
    }

    /**
     * Updates file in the player
     *
     * @param fileToPlay file to start playing
     */
    public void updateFile(String fileToPlay) {
        if (currentFileName != null) {
            NotesDatabase.getInstance().unregisterChildEventListener(this, currentFileName);
        }
        currentFileName = fileToPlay;
        NotesDatabase.getInstance().registerChildEventListener(this, fileToPlay);
        podcastPlayer.start(playerView, fileToPlay);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // TODO: Add snack bar pop up to modify snapshot
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        // No-op
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        // No-op
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        // No-op
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // No-op
    }
}
