package com.pcm.record.activity;

import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pcm.record.R;
import com.pcm.record.adapter.RecordListAdapter;
import com.pcm.record.services.SoundRecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    //control component
    Button playButton   = null;
    Button recButton    = null;
    Button allButton    = null;
    ListView recList    = null;
    Spinner mPcm        = null;
    Spinner mChannel    = null;
    Spinner mQuality    = null;
    TextView mTimer     = null;
    TextView mTitle     = null;

    //status value
    boolean isRecording     = false;
    boolean isPlaying       = false;
    boolean isAllPlaying    = false;
    int mPcmValue   = 0;
    int mChannelVal = 0;
    int mQualityVal = 0;
    int selectedFile = 0;
    public ArrayList<String> mFilesList    = new ArrayList<>();

    //local class
    Context mContext    = null;
    SoundRecorder record    = null;
    MediaPlayer player  = null;
    RecordListAdapter adapter = null;

    public static RecordActivity mInstance = null;

    long elapseTime     = 0;
    Handler timeCounter = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTimer.setText(String.format("%02d", elapseTime / 60000) + ":" + String.format("%02d", elapseTime / 1000));
            elapseTime += 1000;
            timeCounter.sendEmptyMessageDelayed(0, 1000);
        }
    };

    Handler mFileScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            scanBaseDirectory(SoundRecorder.baseDir);
        }
    };

    Handler mTitleChangeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTitle.setText(msg.getData().getString("title"));
        }
    };

    public void scanDirectory()
    {
        mFileScanHandler.sendEmptyMessage(1234);
    }
    public void setTitleChange(String title)
    {
        Message message = new Message();
        Bundle bundler = new Bundle();
        bundler.putString("title", title);
        message.setData(bundler);
        mTitleChangeHandler.sendMessage(message);
    }

    private void scanBaseDirectory(String path)
    {
        File directory = new File(path);
        if (!directory.exists())
        {
            directory.mkdir();
        }

        File[] files = directory.listFiles();
        mFilesList.clear();
        for (int i = 0; i < files.length; i ++)
        {
            mFilesList.add(files[i].getName());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mContext = this;
        mInstance = this;

        playButton  = (Button)this.findViewById(R.id.play);
        recButton  = (Button)this.findViewById(R.id.record);
        allButton = (Button)this.findViewById(R.id.continuePlay);
        recList = (ListView)this.findViewById(R.id.recordList);
        mPcm    = (Spinner)this.findViewById(R.id.pcm);
        mChannel   = (Spinner)this.findViewById(R.id.channel);
        mQuality   = (Spinner)this.findViewById(R.id.quality);
        mTimer  = (TextView)this.findViewById(R.id.timer);
        mTitle  = (TextView)this.findViewById(R.id.title);

        playButton.setOnClickListener(this);
        recButton.setOnClickListener(this);
        allButton.setOnClickListener(this);

        setDropDownPcm();
        setDropDownQuality();
        setDropDownChannel();

        adapter = new RecordListAdapter(mContext, mFilesList);
        recList.setAdapter(adapter);

        recList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTitle.setText(mFilesList.get(position));
                selectedFile = position;
            }

        });

        scanBaseDirectory(SoundRecorder.baseDir);

        mPcm.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPcmValue = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mQuality.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mQualityVal = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mChannel.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChannelVal = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setDropDownChannel() {
        List<String> list = new ArrayList<String>();
        list.add("Mono");
        list.add("Stereo");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChannel.setAdapter(dataAdapter);
    }

    private void setDropDownQuality() {
        List<String> list = new ArrayList<String>();
        list.add("Base");
        list.add("Normal");
        list.add("High");
        list.add("Superior");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mQuality.setAdapter(dataAdapter);
    }

    private void setDropDownPcm() {
        List<String> list = new ArrayList<String>();
        list.add("PCM16");
        list.add("PCM8");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPcm.setAdapter(dataAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(playButton))
        {
            if (!isPlaying)
            {
                if (startPlaying())
                    setPlayRecStatus(true, false, false);
            }
            else
            {
                stopPlaying();
                setPlayRecStatus(false, false, false);
            }
        }
        else if (view.equals(recButton))
        {
            if (isRecording)
            {
                stopRecording();
                setPlayRecStatus(false, false, false);
            }
            else
            {
                startRecording();
                setPlayRecStatus(false, true, false);
            }
        }
        else if (view.equals(allButton))
        {
            if (isAllPlaying)
            {
                stopAllPlaying();
                setPlayRecStatus(false, false, false);
            }
            else
            {
                if (startAllPlaying())
                    setPlayRecStatus(false, false, true);
            }
        }
    }

    private ArrayList<String> getFilesList(int index)
    {
        ArrayList<String> lists = new ArrayList<>();

        for (int i = index; i < mFilesList.size(); i ++)
        {
            if (mFilesList.get(i).contains(".wav"))
            {
                break;
            }
            lists.add(mFilesList.get(i));
        }
        return lists;
    }

    private boolean startAllPlaying()
    {
        if (mFilesList.size() > 0)
        {
            if (mFilesList.get(selectedFile).contains(".wav"))
            {
                player = MediaPlayer.create(this, Uri.fromFile(new File(SoundRecorder.baseDir + mFilesList.get(selectedFile))));
                player.start();

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        timeCounter.removeMessages(0);
                        setPlayRecStatus(false, false, false);
                        player = null;
                    }
                });
            }
            else
            {
                record = new SoundRecorder(mContext, mFilesList.get(selectedFile), new SoundRecorder.OnVoicePlaybackStateChangedListener() {
                    @Override
                    public void onPlaybackStopped() {
                        timeCounter.removeMessages(0);
                        setPlayRecStatus(false, false, false);
                        record = null;
                    }
                });

                record.startAllPlay(getFilesList(selectedFile));
            }

            mTimer.setText("00:00");
            mTitle.setText(mFilesList.get(selectedFile));

            elapseTime = 1000;
            timeCounter.sendEmptyMessageDelayed(0, 1000);

            return true;
        }
        return false;
    }

    private void stopAllPlaying()
    {
        if (player != null && isAllPlaying)
        {
            player.stop();
            timeCounter.removeMessages(0);
            setPlayRecStatus(false, false, false);
            player = null;
        }
        if (record != null && isAllPlaying)
        {
            record.stopPlaying();
            record = null;
        }
    }

    private void stopPlaying() {
        if (player != null && isPlaying)
        {
            player.stop();
            timeCounter.removeMessages(0);
            setPlayRecStatus(false, false, false);
            player = null;
        }
        if (record != null && isPlaying)
        {
            record.stopPlaying();
            record = null;
        }
    }

    private boolean startPlaying() {
        if (mFilesList.size() > 0)
        {
            if (mFilesList.get(selectedFile).contains(".wav"))
            {
                player = MediaPlayer.create(this, Uri.fromFile(new File(SoundRecorder.baseDir + mFilesList.get(selectedFile))));
                player.start();

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        timeCounter.removeMessages(0);
                        setPlayRecStatus(false, false, false);
                        player = null;
                    }
                });
            }
            else
            {
                record = new SoundRecorder(mContext, mFilesList.get(selectedFile), new SoundRecorder.OnVoicePlaybackStateChangedListener() {
                    @Override
                    public void onPlaybackStopped() {
                        timeCounter.removeMessages(0);
                        setPlayRecStatus(false, false, false);
                        record = null;
                    }
                });
                record.startPlay();
            }

            mTimer.setText("00:00");
            mTitle.setText(mFilesList.get(selectedFile));

            elapseTime = 1000;
            timeCounter.sendEmptyMessageDelayed(0, 1000);

            return true;
        }
        return false;
    }

    private void setRecordConfig()
    {
        if (mChannelVal == 0)
        {
            SoundRecorder.CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
            SoundRecorder.CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
        }
        else
        {
            SoundRecorder.CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
            SoundRecorder.CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
        }

        if (mPcmValue == 0)
        {
            SoundRecorder.FORMAT = AudioFormat.ENCODING_PCM_16BIT;
            SoundRecorder.extenstion = ".pcm16";
        }
        else
        {
            SoundRecorder.FORMAT = AudioFormat.ENCODING_PCM_8BIT;
            SoundRecorder.extenstion = ".pcm18";
        }

        if (mQualityVal == 0)
        {
            SoundRecorder.RECORDING_RATE = 8000;
        }
        else if (mQualityVal == 1)
        {
            SoundRecorder.RECORDING_RATE = 16000;
        }
        else if (mQualityVal == 2)
        {
            SoundRecorder.RECORDING_RATE = 22050;
        }
        else if (mQualityVal == 2)
        {
            SoundRecorder.RECORDING_RATE = 44100;
        }

        SoundRecorder.BUFFER_SIZE = AudioRecord
                .getMinBufferSize(SoundRecorder.RECORDING_RATE, SoundRecorder.CHANNEL_IN , SoundRecorder.FORMAT);
    }

    private void startRecording() {
        setRecordConfig();
        selectedFile = 0;
        String format = "yyyy_MM_dd_HHMMSS";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

        String fileName     = "rec_" + sdf.format(new Date());

        record = new SoundRecorder(mContext, fileName, new SoundRecorder.OnVoicePlaybackStateChangedListener() {
            @Override
            public void onPlaybackStopped() {
                setPlayRecStatus(false, false, false);
                timeCounter.removeMessages(0);
                record = null;
            }
        });

        mTimer.setText("00:00");
        record.startRecording();

        elapseTime = 1000;
        timeCounter.sendEmptyMessageDelayed(0, 1000);
    }

    private void stopRecording() {
        if (record != null && isRecording)
        {
            record.stopRecording();
            timeCounter.removeMessages(0);
            record = null;

            Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    scanBaseDirectory(SoundRecorder.baseDir);
                }
            };
            mHandler.sendEmptyMessageDelayed(100, 200);
        }
    }

    private void setPlayRecStatus(boolean playing, boolean isRec, boolean allPlaying)
    {
        isPlaying   = playing;
        isRecording = isRec;
        isAllPlaying = allPlaying;

        if (isPlaying)
        {
            playButton.setEnabled(true);
            recButton.setEnabled(false);
            allButton.setEnabled(false);
            playButton.setText(mContext.getString(R.string.stop));
            recButton.setText(mContext.getString(R.string.record));
            allButton.setText(mContext.getString(R.string.playcontinue));
        }
        else
        {
            playButton.setText(mContext.getString(R.string.play));
            recButton.setEnabled(true);
            allButton.setEnabled(true);
        }

        if (isRecording)
        {
            playButton.setEnabled(false);
            recButton.setEnabled(true);
            allButton.setEnabled(false);
            playButton.setText(mContext.getString(R.string.play));
            allButton.setText(mContext.getString(R.string.playcontinue));
            recButton.setText(mContext.getString(R.string.stop));
        }
        else
        {
            recButton.setText(mContext.getString(R.string.record));
            playButton.setEnabled(true);
            allButton.setEnabled(true);
        }

        if (isAllPlaying)
        {
            playButton.setEnabled(false);
            recButton.setEnabled(false);
            allButton.setEnabled(true);
            playButton.setText(mContext.getString(R.string.play));
            allButton.setText(mContext.getString(R.string.stop));
            recButton.setText(mContext.getString(R.string.record));
        }
        else
        {
            allButton.setText(mContext.getString(R.string.playcontinue));
            playButton.setEnabled(true);
            recButton.setEnabled(true);
        }
    }
}
