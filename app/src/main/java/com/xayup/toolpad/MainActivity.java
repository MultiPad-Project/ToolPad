package com.xayup.toolpad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.*;
import androidx.appcompat.content.res.AppCompatResources;
import com.google.android.glass.sample.waveform.WaveformView;
import com.xayup.audio.AudioFile;
import com.xayup.multipad.VariaveisStaticas;
import com.xayup.multipad.XayUpFunctions;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.skin.SkinManager;
import com.xayup.toolpad.global.Defaults;
import com.xayup.toolpad.leds.Leds;
import com.xayup.toolpad.leds.frames.Frame;
import com.xayup.toolpad.leds.frames.Frames;
import com.xayup.toolpad.leds.frames.FramesGroups;
import com.xayup.toolpad.global.GlobalSettings;
import com.xayup.widgets.frames.FrameView;
import com.xayup.widgets.frames.FramesLayout;
import com.xayup.widgets.seekbar.CustomSeekBar;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    protected final short META_STATE_RELOAD_FRAMES_TO_VIEW = 1;

    private final Activity context = this;

    //PROJECT
    public String project_name = "Test";
    public String project_sounds = "";
    public String project_keyled = "";

    // MAKE LEDS SCREEN
    RelativeLayout root;
    //Top left panel
    private View topLeftPanel;
    ///Frames timeline
    private View waveformVisualizerContainer;
    private View framesTimeLineControllerBackground;
    private WaveformView waveForm;
    ///Frames Layout
    private View framesBackground;
    private FrameView currentFrameView, currentFramesGroupView;
    private View framesHorizontalZoom, framesVerticalZoom, framesLimitOut;
    private TextView framesHorizontalZoomText, framesVerticalZoomText;

    //Bottom left panel
    private View bottomLeftPanel;
    ///Leds layers on pad
    private View padLightsLayersLayout;
    private ListView padLightsLayersList;
    private Button btPadLightsLayerAdd;
    ///Colors panel
    private GridLayout colorsGrid;
    private View colorsGridLayout;
    private View colorsHexLayout;
    private View colorsLeftPanelLayout;
    private ListView frameLightsList;
    private Button btColorVelocity, btColorHEX;


    public View makeLedsContainer;
    private MakePads.Pads grid;
    private FramesLayout led_frames, led_frames_groups;
    private LinearLayout led_frames_background;
    private HorizontalScrollView led_frames_horizontal;
    private ScrollView led_frames_vertical;
    private int root_h = 0;
    private CustomSeekBar led_frames_progress;
    private View led_frames_vertical_bar;
    private Button led_frames_prev, led_frames_pause_play, led_frames_next, btFramesBack, btClearLeds;
    private EditText currentStepsCounts_edit, currentStepsSpamCounts_edit;


    public Leds leds;
    public FramesGroups currentFramesGroups;
    public Frames currentFrames;
    public Frame currentFrame;

    public FramesLayout currentLedsFramesLayout;

    public int currentStepsSpamCounts, currentStepsCounts, currentMsTime;

    private final int OP_DISABLED = 0;
    private final int OP_SELECT_PAD = 1;
    private final int OP_MAKE_LEDS = 2;
    private final int OP_TEST_LEDS = 3;

    public int OPERATION;

    public int current_chain_mc;
    /**
     * current_pad [ROW, COLUM]
     */
    public int[] current_pad;

    public int color = 0;

    /*
     * Soma ou subtraia com a id para obter um certo valor
     * util para obter uma view a partir da ID que iniciaria com uma
     * ID que seria usando em outra view (x e y da Grid, codigo de cor) ou
     * identificar o tipo de retorno de um metodo.
     */
    private final int ID_COLOR = 0;
    private final int ID_FRAME = 1;


    //

    // SAMPLE
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove this line if you don't want AndroidIDE to show this app's logs
        super.onCreate(savedInstanceState);
        // Inflate and get instance of binding
        String errLog = TopExceptionHandler.getErrorLog(this);
        if (errLog == null) {
            setContentView(R.layout.activity_main);
            mainActivity();
        } else {
            setContentView(R.layout.crash);
            ((TextView) findViewById(R.id.logText)).setText(errLog);
        }
    }

    public void mainActivity(){
        getSupportActionBar().hide();
        XayUpFunctions.hideSystemBars(getWindow());
        loadGlobalSettingFromResources(super.getApplicationContext());
        setupDirectoryes();
        setupMakeLeds();
        makeLedsContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed(){
        View mainMenu = getLayoutInflater().inflate(R.layout.main_menu, null);
        AlertDialog dialog = new AlertDialog.Builder(context).setView(mainMenu).create();
        mainMenu.findViewById(R.id.main_menu_app_exit).setOnClickListener((view)->{
            dialog.dismiss();
            super.onBackPressed();
        });
        dialog.show();
    }

    public void loadGlobalSettingFromResources(Context context){
        GlobalSettings.frame_color = context.getColor(R.color.frame_color);
        GlobalSettings.frame_selected_color = context.getColor(R.color.frame_selected_color);
        GlobalSettings.frames_group_color = context.getColor(R.color.frames_group_color);
        GlobalSettings.frames_group_selected_color = context.getColor(R.color.frames_group_selected_color);
    }

    public void markPadsWithLedMapped(MakePads.Pads grid, int current_chain_mc, boolean mark_on){
        List<FramesGroups> fgList = leds.getFramesGroupsOnChain(current_chain_mc);
        FramesGroups fg;
        while(!fgList.isEmpty()){
            fg = fgList.remove(0);
            grid.setLedColor(fg.padRow, fg.padColum, (mark_on) ? GlobalSettings.pad_with_led_mapped_color : 0);
        }
    }

    public void showFrameLight(Frame frame, MakePads.Pads grid){
        //grid.clearLeds();
        for(int i = 0; i < frame.getList().size(); i++){
            int[] light = frame.getList().get(i);
            grid.setLedColor(light[Frame.INDEX_ROW], light[Frame.INDEX_COLUM], VariaveisStaticas.newColorInt[light[Frame.INDEX_VALUE]]);
        }
    }

    public void setupDirectoryes(){
        project_keyled = Defaults.APP_EXTERNAL_DIRECTORY_PROJECTS + File.separator + project_name + File.separator + Defaults.PROJECT_FOLDER_NAME_KEYLEDS;
        project_sounds = Defaults.APP_EXTERNAL_DIRECTORY_PROJECTS + File.separator + project_name + File.separator + Defaults.PROJECT_FOLDER_NAME_SAMPLE;
        new File(project_keyled).mkdirs();
        new File(project_sounds).mkdirs();
    }

    public void updateTimeLine() {
        waveformVisualizerContainer.getLayoutParams().width = currentLedsFramesLayout.getColumnsWidth() * (currentStepsCounts + currentStepsSpamCounts);
        waveformVisualizerContainer.requestLayout();
        waveForm.getLayoutParams().width = currentLedsFramesLayout.getColumnsWidth() * (currentStepsCounts);
        waveForm.requestLayout();
        led_frames_progress.setMax(currentStepsCounts + currentStepsSpamCounts);

    }

    public void loadSample(File sample){
        try {
            //waveForm.updateAudioData(AudioFile.readyAudioFile(sample));

            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(sample.getPath());
            mMediaPlayer.prepare();
            currentMsTime = mMediaPlayer.getDuration();
            currentStepsCounts = currentMsTime;
            currentStepsCounts_edit.setText(String.valueOf(currentStepsCounts));
            currentStepsSpamCounts = 5;
            currentStepsSpamCounts_edit.setText(String.valueOf(currentStepsSpamCounts));
        } catch (IOException ie){ Log.v("Waveform error", ie.toString()); }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupMakeLeds() {
        makeLedsContainer = findViewById(R.id.make_leds_root);
        led_frames = findViewById(R.id.led_frames);
        led_frames_groups = findViewById(R.id.led_frames_groups);
        led_frames_background = findViewById(R.id.led_frames_background);
        led_frames_horizontal = findViewById(R.id.led_frames_horizontal);
        led_frames_vertical = findViewById(R.id.led_frames_vertical);
        led_frames_progress = findViewById(R.id.led_frames_progress);
        led_frames_vertical_bar = findViewById(R.id.led_frames_verticalbar);
        colorsGrid = findViewById(R.id.colors_list);
        colorsGridLayout = findViewById(R.id.colors_list_background);
        colorsHexLayout = findViewById(R.id.make_leds_colors_hex_background);
        led_frames_prev = findViewById(R.id.led_frames_prev);
        led_frames_pause_play = findViewById(R.id.led_frames_pause_play);
        led_frames_next = findViewById(R.id.led_frames_next);
        currentStepsCounts_edit = findViewById(R.id.bpm_edit);
        currentStepsSpamCounts_edit = findViewById(R.id.time_edit);
        padLightsLayersList = findViewById(R.id.make_leds_pad_leds_layers);
        padLightsLayersLayout = findViewById(R.id.make_leds_pad_leds_layers_background);
        btPadLightsLayerAdd = findViewById(R.id.make_leds_bt_pad_leds_layers_add);
        led_frames_progress.setPadding(0, 0, 0, 0);
        btFramesBack = findViewById(R.id.make_leds_bt_frames_back);
        colorsLeftPanelLayout = findViewById(R.id.make_leds_colors_controllers);
        topLeftPanel = findViewById(R.id.make_leds_top_left_panel);
        framesTimeLineControllerBackground = findViewById(R.id.make_leds_frames_timeline_control);
        waveForm = findViewById(R.id.make_leds_waveformview);
        waveformVisualizerContainer = findViewById(R.id.led_frame_sample_visualizer_container);
        framesLimitOut = findViewById(R.id.make_leds_frames_limit_out);
        framesBackground = findViewById(R.id.led_frames_view_background);

        root = findViewById(R.id.main_background);
        leds = new Leds();
        current_pad = new int[2];

        loadSample(new File(Defaults.PROJECT_FOLDER_NAME_SAMPLE, "sample.wav"));

        (framesHorizontalZoom = findViewById(R.id.make_leds_bt_slide_frames_horizontal_zoom)).setOnTouchListener(
                new View.OnTouchListener() {
                    boolean zoom = false;
                    float start_with = 0;
                    float started_offset = 0;
                    final View floating_text = context.getLayoutInflater().inflate(R.layout.floating_text, null);
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                            start_with = currentLedsFramesLayout.getDefaultFrameViewWidth();
                            ((TextView) floating_text.findViewById(android.R.id.text1)).setText(String.valueOf(currentLedsFramesLayout.getVisibleColumns()));
                            root.addView(floating_text);
                        } else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                            if(zoom){
                                if((motionEvent.getX()-started_offset) > start_with){
                                    if(currentLedsFramesLayout.getColumnsCount() >= currentLedsFramesLayout.getVisibleColumns() + 1){
                                        currentLedsFramesLayout.setVisibleColumns(currentLedsFramesLayout.getVisibleColumns() + 1, led_frames_horizontal.getMeasuredWidth());
                                        started_offset = motionEvent.getX();
                                        ((TextView) floating_text.findViewById(android.R.id.text1)).setText(String.valueOf(currentLedsFramesLayout.getVisibleColumns()));
                                        currentLedsFramesLayout.requestLayout();
                                        updateTimeLine();
                                    }
                                } else if ((started_offset-motionEvent.getX()) > start_with){
                                    if(currentLedsFramesLayout.getColumnsCount() > currentLedsFramesLayout.getVisibleColumns() - 1){
                                        currentLedsFramesLayout.setVisibleColumns(currentLedsFramesLayout.getVisibleColumns() - 1, led_frames_horizontal.getMeasuredWidth());
                                        started_offset = motionEvent.getX();
                                        ((TextView) floating_text.findViewById(android.R.id.text1)).setText(String.valueOf(currentLedsFramesLayout.getVisibleColumns()));
                                        currentLedsFramesLayout.requestLayout();
                                        updateTimeLine();
                                    }
                                }
                            } else if (motionEvent.getX() > start_with || motionEvent.getX() < -start_with){
                                started_offset = motionEvent.getX();
                                zoom = true;
                            }
                            //return true;
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            zoom = false;
                            root.removeView(floating_text);
                            return false;
                        }
                        return true;
                    }
                }
        );
(framesVerticalZoom = findViewById(R.id.make_leds_bt_slide_frames_vertical_zoom)).setOnTouchListener(
                new View.OnTouchListener() {
                    boolean zoom = false;
                    float start_with = 0;
                    float started_offset = 0;
                    final View floating_text = context.getLayoutInflater().inflate(R.layout.floating_text, null);
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                            start_with = view.getMeasuredWidth();
                            ((TextView) floating_text.findViewById(android.R.id.text1)).setText(String.valueOf(currentLedsFramesLayout.getVisibleRows()));
                            root.addView(floating_text);
                        } else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                            if(zoom){
                                if((motionEvent.getY()-started_offset) > start_with){
                                    if(currentLedsFramesLayout.getRowsCount() < currentLedsFramesLayout.getVisibleRows() + 1)
                                        currentLedsFramesLayout.setRowsCount(currentLedsFramesLayout.getRowsCount()+1);
                                    currentLedsFramesLayout.setVisibleRows(currentLedsFramesLayout.getVisibleRows() + 1, led_frames_horizontal.getMeasuredHeight()-led_frames_progress.getMeasuredHeight());
                                    started_offset = motionEvent.getY();
                                    ((TextView) floating_text.findViewById(android.R.id.text1)).setText(String.valueOf(currentLedsFramesLayout.getVisibleRows()));
                                    currentLedsFramesLayout.requestLayout();

                                } else if ((started_offset-motionEvent.getY()) > start_with){
                                    if(currentLedsFramesLayout.getRowsCount() > currentLedsFramesLayout.getVisibleRows() - 1){
                                        currentLedsFramesLayout.setVisibleRows(currentLedsFramesLayout.getVisibleRows() - 1, led_frames_horizontal.getMeasuredHeight()-led_frames_progress.getMeasuredHeight());
                                        started_offset = motionEvent.getY();
                                        ((TextView) floating_text.findViewById(android.R.id.text1)).setText(String.valueOf(currentLedsFramesLayout.getVisibleRows()));
                                        currentLedsFramesLayout.requestLayout();
                                    }
                                }
                            } else if (motionEvent.getY() > start_with || motionEvent.getY() < -start_with){
                                started_offset = motionEvent.getY();
                                zoom = true;
                            }
                            //return true;
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            zoom = false;
                            root.removeView(floating_text);
                            return false;
                        }
                        return true;
                    }
                }
        );

        (findViewById(R.id.write)).setOnClickListener((view)->{
            if(currentFramesGroups != null){
                File file = new File(project_keyled, current_chain_mc + " " + current_pad[0] + " " + current_pad[1] + " 1");
                try {
                    file.getParentFile().mkdirs();
                    if(file.exists()) file.delete();
                    if(file.createNewFile())
                        Log.v("Write Leds sucess", String.valueOf(currentFramesGroups.writeToFile(currentStepsCounts, file)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        (btColorVelocity = findViewById(R.id.make_leds_bt_colors_velocity)).setOnClickListener((view)->{
            colorsHexLayout.setVisibility(View.GONE);
            colorsGridLayout.setVisibility(View.VISIBLE);
        });
        (btColorHEX = findViewById(R.id.make_leds_bt_colors_hex)).setOnClickListener((view)->{
            colorsHexLayout.setVisibility(View.VISIBLE);
            colorsGridLayout.setVisibility(View.GONE);
        });
        (btClearLeds = findViewById(R.id.make_leds_bt_clear_leds)).setOnClickListener((view)-> grid.clearLeds());
        (frameLightsList = findViewById(R.id.make_leds_list_frame_lights)).setAdapter(new BaseAdapter() {
            List<int[]> list = (currentFrame != null) ? currentFrame.getList() : null;
            @Override
            public void notifyDataSetChanged() {
                list = (currentFrame != null) ? currentFrame.getList() : null;
                super.notifyDataSetChanged();
            }
            @Override
            public int getCount() {
                return (list != null) ? list.size() : 0;
            }
            @Override
            public int[] getItem(int i) {
                return (list != null) ? list.get(i) : null;
            }
            @Override
            public long getItemId(int i) {
                return i;
            }
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view == null) view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                String text = "";
                int[] frame = getItem(i);

                if (frame[Frame.INDEX_ROW] == 0 && frame[Frame.INDEX_COLUM] == 9) {
                    text += Frame.LightTypeText.TYPE_LOGO;
                } else {
                    switch (frame[Frame.INDEX_TYPE]){
                        case Frame.LightType.TYPE_ON:{
                            text += Frame.LightTypeText.TYPE_ON;
                            text += " " + frame[Frame.INDEX_ROW] + " " + frame[Frame.INDEX_COLUM] + " " + ((frame[Frame.INDEX_VALUE] < 128) ? "a " + frame[Frame.INDEX_VALUE] : frame[Frame.INDEX_VALUE]);
                            break;
                        }
                        case Frame.LightType.TYPE_OFF:{
                            text += Frame.LightTypeText.TYPE_OFF;
                            text += " " + frame[Frame.INDEX_ROW] + " " + frame[Frame.INDEX_COLUM];
                            break;
                        }
                        case Frame.LightType.TYPE_DELAY:{
                            text += Frame.LightTypeText.TYPE_DELAY;
                            text += " " + frame[Frame.INDEX_VALUE];
                            break;
                        }
                    }
                }
                ((TextView) view.findViewById(android.R.id.text1)).setText(text);
                return view;
            }
        });
        frameLightsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentFrames != null){
                    currentFrame.getList().remove(adapterView.getItemAtPosition(i));
                    ((BaseAdapter) frameLightsList.getAdapter()).notifyDataSetChanged();
                }
                return false;
            }
        });

        //currentStepsSpamCounts = Integer.parseInt(currentStepsSpamCounts_edit.getText().toString());
        currentStepsSpamCounts_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()) {
                    int tmp = Integer.parseInt(charSequence.toString());
                    currentStepsSpamCounts = Math.max(Math.max(tmp, currentMsTime), 0);
                    //UPDATE FRAMES SIZE
                    led_frames_groups.setColumnsCount(currentStepsCounts + currentStepsSpamCounts);
                    led_frames.setColumnsCount(currentStepsCounts + currentStepsSpamCounts);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //currentStepsCounts = Integer.parseInt(currentStepsCounts_edit.getText().toString());
        currentStepsCounts_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()) {
                    int tmp = Integer.parseInt(charSequence.toString());
                    currentStepsCounts = Math.min(tmp, currentStepsCounts + currentStepsSpamCounts);
                    //UPDATE FRAMES SIZE
                    led_frames_groups.setColumnsCount(currentStepsCounts + currentStepsSpamCounts);
                    led_frames.setColumnsCount(currentStepsCounts + currentStepsSpamCounts);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        makeLedsContainer.post(
                new Runnable() {
                    @Override
                    public void run() {
                        root_h = makeLedsContainer.getMeasuredHeight();
                        grid = new MakePads(context).make((byte) 10, (byte) 10, (padView, padInfo)->{
                            ImageView btn_ = padView.findViewById(MakePads.PadInfo.PadLayerType.BTN_);
                            if(btn_ != null) btn_.setVisibility(View.GONE);
                            padView.setOnClickListener((view)->{
                                if(padInfo.getType() == MakePads.PadType.CHAIN || padInfo.getType() == MakePads.PadType.PAD_LOGO){
                                    if(OPERATION == OP_TEST_LEDS || OPERATION == OP_SELECT_PAD) {
                                        if(padInfo.getType() == MakePads.PadType.PAD_LOGO) return;
                                        int[] cXY = MakePads.PadID.getChainXY(current_chain_mc, 9);
                                        grid.padWatermark(cXY[0], cXY[1], false);
                                        markPadsWithLedMapped(grid, current_chain_mc, false);
                                        current_chain_mc = ((MakePads.ChainInfo) padInfo).getMc();
                                        cXY = MakePads.PadID.getChainXY(current_chain_mc, 9);
                                        grid.padWatermark(cXY[0], cXY[1], true);
                                        markPadsWithLedMapped(grid, current_chain_mc, true);
                                    } else if (currentFrame != null){
                                        if(padInfo.getType() == MakePads.PadType.PAD_LOGO){
                                            currentFrame.addLight(Frame.LightType.TYPE_LOGO, padInfo.getRow(), padInfo.getColum(), color);
                                        } else {
                                            currentFrame.addLight(color == 0 ? Frame.LightType.TYPE_OFF : Frame.LightType.TYPE_ON, padInfo.getRow(), padInfo.getColum(), color);
                                        }
                                        grid.setLedColor(padInfo.getRow(), padInfo.getColum(), VariaveisStaticas.newColorInt[color]);
                                    }
                                } else if(padInfo.getType() == MakePads.PadType.PAD){
                                    if(OPERATION == OP_SELECT_PAD) {
                                        if (current_pad[0] != 0 && current_pad[1] != 0) {
                                            grid.padWatermark(current_pad[0], current_pad[1], false);
                                        }
                                        current_pad[0] = padInfo.getRow();
                                        current_pad[1] = padInfo.getColum();
                                        grid.padWatermark(current_pad[0], current_pad[1], true);

                                        padLightsLayersList.setAdapter(new BaseAdapter() {
                                            List<FramesGroups> list = leds.getFramesGroupsOn(current_chain_mc, current_pad[0], current_pad[1]);
                                            @Override
                                            public void notifyDataSetChanged() {
                                                list = leds.getFramesGroupsOn(current_chain_mc, current_pad[0], current_pad[1]);
                                                super.notifyDataSetChanged();
                                            }
                                            @Override
                                            public int getCount() {
                                                if(list != null) return list.size();
                                                return 0;
                                            }
                                            @Override
                                            public FramesGroups getItem(int i) { return (list != null) ? list.get(i) : null; }
                                            @Override
                                            public long getItemId(int i) { return i; }
                                            @Override
                                            public View getView(int i, View view, ViewGroup viewGroup) {
                                                if(view == null) view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                                                ((TextView) view.findViewById(android.R.id.text1)).setText(String.valueOf(current_pad[0] + " " + current_pad[1] + " " + i));
                                                return view;
                                            }
                                        });
                                        configureInterfacePadLightsLayers();
                                    } else if (currentFrame != null){
                                        Log.v("Pad to light color", String.valueOf(color));
                                        grid.setLedColor(padInfo.getRow(), padInfo.getColum(), VariaveisStaticas.newColorInt[color]);
                                        currentFrame.addLight(color == 0 ? Frame.LightType.TYPE_OFF : Frame.LightType.TYPE_ON, padInfo.getRow(), padInfo.getColum(), color);
                                        ((BaseAdapter) frameLightsList.getAdapter()).notifyDataSetChanged();
                                    }
                                }
                            });
                        });
                        ((ViewGroup) findViewById(R.id.pads)).addView(grid.getRoot(), new ViewGroup.LayoutParams(root_h, root_h));
                        SkinManager.updateSkin(context, grid, BuildConfig.APPLICATION_ID);
                        OPERATION = OP_SELECT_PAD;
                        grid.getPadView(1, 9).callOnClick();
                    }
                });


        led_frames_horizontal.post(()->{
            //findViewById(R.id.make_leds_frames_container).getLayoutParams().width = led_frames_horizontal.getMeasuredWidth();
            led_frames.getLayoutParams().height = led_frames_horizontal.getMeasuredHeight();
            led_frames.getLayoutParams().width = led_frames_horizontal.getMeasuredWidth();
            led_frames.getLayoutParams().height = led_frames_horizontal.getMeasuredHeight();
            led_frames.requestLayout();
            led_frames.post(()->{
                led_frames.setColumnsCount(currentStepsCounts + currentStepsSpamCounts);
                led_frames.setRowsCount(GlobalSettings.frames_visible_rows_count);
                led_frames.setVisibleFrames(led_frames.getRowsCount(), GlobalSettings.frames_visible_columns_count, led_frames_horizontal.getMeasuredWidth(), led_frames_horizontal.getMeasuredHeight()-led_frames_progress.getMeasuredHeight());
                led_frames.requestLayout();
                led_frames_progress.setMax(led_frames.getColumnsCount());
            });

            //TESTE
            currentLedsFramesLayout = led_frames_groups;
            //

            led_frames_groups.getLayoutParams().width = led_frames_horizontal.getMeasuredWidth();
            led_frames_groups.getLayoutParams().height = led_frames_horizontal.getMeasuredHeight();
            led_frames_groups.requestLayout();
            led_frames_groups.post(()->{
                led_frames_groups.setColumnsCount(currentStepsCounts + currentStepsSpamCounts);
                led_frames_groups.setRowsCount(GlobalSettings.frames_visible_rows_count);
                led_frames_groups.setVisibleFrames(GlobalSettings.frames_visible_rows_count, GlobalSettings.frames_visible_columns_count, led_frames_horizontal.getMeasuredWidth(), led_frames_horizontal.getMeasuredHeight()-led_frames_progress.getMeasuredHeight());
                led_frames_groups.setDefaultFrameViewWidth(led_frames_groups.getColumnsWidth());
                //led_frames_groups.setDefaultFrameViewWidth(led_frames_groups.getColumnsWidth());
                led_frames_groups.requestLayout();
                updateTimeLine();
                //led_frames_progress.setMax(led_frames_groups.getColumnsCount());
            });

            led_frames_background.requestLayout();
            led_frames_progress.requestLayout();
            led_frames_progress.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        int vertical_bar_offset = 0;
                        int progress_length = 0;

                        public void updateVerticalBar(int progress){
                            runOnUiThread(()->{
                                ((ViewGroup.MarginLayoutParams)
                                        led_frames_vertical_bar.getLayoutParams())
                                    .setMarginStart((progress_length * progress) - vertical_bar_offset);
                                led_frames_vertical_bar.requestLayout();
                            });
                        }

                        @Override
                        public void onProgressChanged(
                                SeekBar seekBar, int progress, boolean fromUser) {
                            updateVerticalBar(progress);
                            if(((CustomSeekBar) seekBar).getData() == null) return;
                            if(fromUser) grid.clearLeds();
                            new Thread(()-> {
                                List<int[]> framesList;
                                for(int i = (fromUser) ? -1 : progress-1; i < progress; i++)
                                    if( i > -1 && (framesList = ((CustomSeekBar) seekBar).getData().get(i)) != null)
                                        while (!framesList.isEmpty()) {
                                            int[] frame = framesList.remove(0);
                                            Log.v("Frame array", Arrays.toString(frame));
                                            switch (frame[Frame.INDEX_TYPE]) {
                                                case Frame.LightType.TYPE_DELAY:
                                                    long delay = frame[Frame.INDEX_VALUE] + SystemClock.uptimeMillis();
                                                    while (SystemClock.uptimeMillis() < delay) ;
                                                    break;
                                                case Frame.LightType.TYPE_ON:
                                                    runOnUiThread(() -> grid.setLedColor(frame[Frame.INDEX_ROW], frame[Frame.INDEX_COLUM], VariaveisStaticas.newColorInt[frame[Frame.INDEX_VALUE]]));
                                                    break;
                                                case Frame.LightType.TYPE_LOGO:
                                                    runOnUiThread(() -> grid.setLedColor(0, 9, VariaveisStaticas.newColorInt[frame[Frame.INDEX_VALUE]]));
                                                    break;
                                                case Frame.LightType.TYPE_OFF:
                                                    runOnUiThread(() -> grid.setLedColor(frame[Frame.INDEX_ROW], frame[Frame.INDEX_COLUM], 0));
                                                    break;
                                            }
                                        }
                            }).start();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            vertical_bar_offset = led_frames_vertical_bar.getMeasuredWidth() != 0 ? led_frames_vertical_bar.getMeasuredWidth()/2 : 0;
                            progress_length = (led_frames_progress.getMeasuredWidth() != 0 ?
                                    (led_frames_progress.getMax() != 0 ? led_frames_progress.getMeasuredWidth() / led_frames_progress.getMax() : 0) : 0);
                            updateVerticalBar(seekBar.getProgress());
                            mMediaPlayer.seekTo(led_frames_progress.getProgress());
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) { updateVerticalBar(seekBar.getProgress()); }
                    });
        });
        led_frames_groups.setOnTouchListener(new FramesLayout.OnTouch() {
            final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
                    Log.v("Scale gesture", String.valueOf(scaleGestureDetector.getScaleFactor()));
                    return true;
                }
            });
            @Override
            public boolean onTouch(View view, MotionEvent event, int row, int colum) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(currentFramesGroupView != null){
                        currentFramesGroupView.getBackground().setTint(GlobalSettings.frames_group_color);
                    }
                    currentFramesGroupView = led_frames_groups.addFrameView(row, colum);
                    currentFramesGroupView.getLayoutParams().width = (int) (0.2* led_frames_horizontal.getMeasuredWidth());
                    currentFramesGroupView.setBackground(AppCompatResources.getDrawable(context, R.drawable.frame_drawable));
                    currentFramesGroupView.getBackground().setTint(GlobalSettings.frames_group_selected_color);
                    if(event.getMetaState() != META_STATE_RELOAD_FRAMES_TO_VIEW){
                        currentFramesGroupView.setData(currentFrames = currentFramesGroups.newFramesGroup());
                        currentFrames.setOffset(colum, row);
                    }
                    currentFramesGroupView.setOnTouchListener((frame_view, tevent)->{
                        if(tevent.getAction() == MotionEvent.ACTION_UP){
                            if(currentFramesGroupView != null) {
                                currentFramesGroupView.getBackground().setTint(GlobalSettings.frames_group_color);
                                if(currentFramesGroupView == frame_view){
                                    currentFrameView = null;
                                    configureInterfaceFrames();
                                    return true;
                                }
                            }
                            currentFramesGroupView = (FrameView) frame_view;
                            currentFrames = (Frames) currentFramesGroupView.getData();
                            frame_view.getBackground().setTint(GlobalSettings.frames_group_selected_color);
                        } else if(tevent.getAction() == MotionEvent.ACTION_MOVE) {
                            return false;
                        }
                        return true;
                    });
                    view.requestLayout();
                    return true;
                }
                return scaleGestureDetector.onTouchEvent(event);
                //return event.getAction() != MotionEvent.ACTION_MOVE;
            }
        });
        led_frames.setOnTouchListener(new FramesLayout.OnTouch() {
            boolean frame_long_click_called = false;
            @Override
            public boolean onTouch(View view, MotionEvent event, int row, int colum) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    frame_long_click_called = false;
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(!frame_long_click_called){
                        if(currentFrameView != null){
                            currentFrameView.getBackground().setTint(GlobalSettings.frame_color);
                        }
                        currentFrameView = led_frames.addFrameView(row, colum);
                        currentFrameView.setBackground(AppCompatResources.getDrawable(context, R.drawable.frame_drawable));
                        currentFrameView.getBackground().setTint(GlobalSettings.frame_selected_color);
                        if(event.getMetaState() != META_STATE_RELOAD_FRAMES_TO_VIEW) {
                            currentFrameView.setData(currentFrame = currentFrames.newFrame());
                            currentFrame.setOffset(colum, row);
                            showFrameLight(currentFrame, grid);
                        }
                        currentFrameView.setOnTouchListener(new View.OnTouchListener() {
                            long long_click_delay_ms = 0;
                            @Override
                            public boolean onTouch(View frame_view, MotionEvent tevent) {
                                if(tevent.getAction() == MotionEvent.ACTION_DOWN){
                                    frame_long_click_called = false;
                                    long_click_delay_ms = SystemClock.uptimeMillis() + 500;
                                    return true;
                                } else if(tevent.getAction() == MotionEvent.ACTION_UP){
                                    if(!frame_long_click_called){
                                        frame_long_click_called = true;
                                        if (currentFrameView != null) currentFrameView.getBackground().setTint(GlobalSettings.frame_color);
                                        (currentFrameView = (FrameView) frame_view).getBackground().setTint(GlobalSettings.frame_selected_color);
                                        currentFrame = (Frame) currentFrameView.getData();
                                        ((BaseAdapter) frameLightsList.getAdapter()).notifyDataSetChanged();
                                        if(currentFrame != null) {
                                            showFrameLight(currentFrame, grid);
                                        }
                                    }
                                }
                                if(!frame_long_click_called){
                                    if(!(SystemClock.uptimeMillis() < long_click_delay_ms)) {
                                        frame_long_click_called = true;
                                        if(currentFrameView != null && currentFrameView == frame_view) {
                                            led_frames.removeView(currentFrameView);
                                            currentFrames.getList().remove(currentFrameView.getData());
                                            currentFrame = null;
                                            currentFrameView = null;
                                            ((BaseAdapter) frameLightsList.getAdapter()).notifyDataSetChanged();
                                        }
                                    }
                                }
                                return false;
                            }
                        });
                        view.requestLayout();
                    }
                    return false;
                } else return event.getAction() != MotionEvent.ACTION_MOVE;
            }
        });
        led_frames.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if(currentLedsFramesLayout != view) return;
                led_frames_progress.getLayoutParams().width = view.getMeasuredWidth();
                led_frames_progress.requestLayout();
            }
        });
        led_frames_groups.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if(currentLedsFramesLayout != view) return;
                led_frames_progress.getLayoutParams().width = view.getMeasuredWidth();
                led_frames_progress.requestLayout();
            }
        });

        View bottom_left_panel = findViewById(R.id.make_leds_bottom_left_panel_background);
        bottom_left_panel.post(
                () -> {
                    int h = bottom_left_panel.getMeasuredHeight();
                    int items_size = h / 3;
                    int grid_w = 0;
                    final int COLOR_VIEW_ID = 1000;
                    for (int c = 0; c < VariaveisStaticas.newColorInt.length; c += 3) {
                        grid_w += items_size;
                        for (int l = 0;
                                (l < 3 & (l + c) < VariaveisStaticas.newColorInt.length);
                                l++) {
                            //LinearLayout background = new LinearLayout(context);
                            GridLayout.LayoutParams params =
                                    new GridLayout.LayoutParams(
                                            GridLayout.spec(l, GridLayout.FILL, 1.0f),
                                            GridLayout.spec(c, GridLayout.FILL, 1.0f));
                            View color_view = new View(context);
                            color_view.setId(COLOR_VIEW_ID);
                            params.height = 0;
                            params.width = 0;
                            params.setMargins(1, 1, 1, 1);
                            color_view.setBackgroundColor(VariaveisStaticas.newColorInt[l + c]);
                            color_view.setId(l + c);
                            /*
                            background.addView(
                                    color_view,
                                    new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT));

                             */
                            //background.setOnClickListener(onClick(ID_COLOR));
                            colorsGrid.addView(color_view, params);
                            color_view.setOnClickListener((view)-> {
                                //if(color > 0 && color < 128) colors_list.findViewById(color).setBackgroundColor(Color.TRANSPARENT);
                                color = view.getId();
                                //xcolors_list.findViewById(color).setBackgroundColor(GlobalSettings.select_color_color);
                                super.findViewById(R.id.make_leds_color_preview).setBackground(view.getBackground());
                            });
                        }
                    }
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(grid_w, h);
                    colorsGrid.setLayoutParams(params);
                });

       padLightsLayersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentFramesGroups = (FramesGroups) adapterView.getItemAtPosition(i);
                grid.padWatermark(current_pad[0], current_pad[1], false);
                int[] cXY = MakePads.PadID.getChainXY(current_chain_mc, 9);
                grid.padWatermark(cXY[0], cXY[1], false);
                adapterView.setSelection(i);
                configureInterfaceFramesGroups();
                OPERATION = OP_TEST_LEDS;
            }
        });

       btPadLightsLayerAdd.setOnClickListener((view)->{
           leds.newLed(current_chain_mc, current_pad[0], current_pad[1]);
           Log.v("Leds layers size", String.valueOf(1));
           padLightsLayersList.deferNotifyDataSetChanged();
           ((BaseAdapter) padLightsLayersList.getAdapter()).notifyDataSetChanged();
       });

       btFramesBack.setOnClickListener((view)->{
           configureInterfaceFramesGroups();
       });

        led_frames_horizontal.setOnTouchListener(bidirecionalScroll(led_frames_horizontal));
        led_frames_vertical.setOnTouchListener(bidirecionalScroll(led_frames_vertical));
        led_frames_pause_play.setOnClickListener(new View.OnClickListener() {
            boolean playing = false;
            @Override
            public void onClick(View view) {
                if(!playing){
                    playing = true;
                    //led_frames_progress.triggerChangeListener(true);
                    led_frames_progress.setData(led_frames.getVisibility() == View.VISIBLE ? (currentFrames != null ? currentFrames.processAllFramesToOffset(currentStepsCounts) : null)
                            : (led_frames_groups.getVisibility() == View.VISIBLE ? (currentFramesGroups != null ? currentFramesGroups.processAllFramesToOffset(currentStepsCounts) : null) : null));
                    final int ms_delay = Math.round(((float) currentMsTime / currentStepsCounts));//(int) BpmTimer.getMilliseconds(currentSteps, BpmTimer.getBeastPerMilliseconds(currentSteps, currentStepsSpam));
                    if(mMediaPlayer != null){
                        mMediaPlayer.seekTo(led_frames_progress.getProgress());
                        mMediaPlayer.start();
                    }
                    new Thread(()->{
                        long delay = SystemClock.uptimeMillis();
                        while(led_frames_progress.getMax() > led_frames_progress.getProgress()) {
                            led_frames_progress.incrementProgressBy(1);
                            //led_frames_progress.callOnStartTrackingTouch();
                            led_frames_progress.callOnProgressChanged(false);
                            led_frames_progress.callOnStopTrackingTouch();
                            if(led_frames_progress.getMax() == led_frames_progress.getProgress()) break;
                            delay += ms_delay;
                            while (playing && SystemClock.uptimeMillis() < delay);
                            if(!playing) return;
                        }
                        playing = false;
                    }).start();
                } else {
                    playing = false;
                    if(mMediaPlayer != null){
                        mMediaPlayer.stop();
                    }
                }
            }
        });
        configureInterfacePadLightsLayers();
    }

    public void configureInterfacePadLightsLayers(){
        padLightsLayersLayout.setVisibility(View.VISIBLE);
        colorsGridLayout.setVisibility(View.GONE);
        colorsHexLayout.setVisibility(View.GONE);
        colorsGridLayout.setVisibility(View.GONE);
        colorsLeftPanelLayout.setVisibility(View.GONE);
        frameLightsList.setVisibility(View.GONE);
        topLeftPanel.setClickable(false);
        framesTimeLineControllerBackground.setClickable(false);
        //framesTimeLineControllerBackground.setVisibility(View.GONE);
        if (grid != null) {
            grid.padWatermark(current_pad[0], current_pad[1], true);
            int[] cXY = MakePads.PadID.getChainXY(current_chain_mc, 9);
            grid.padWatermark(cXY[0], cXY[1], true);
        }

        OPERATION = OP_SELECT_PAD;
    }

    public void configureInterfaceFrames(){
        led_frames.setVisibility(View.VISIBLE);
        led_frames_groups.setVisibility(View.GONE);
        btFramesBack.setVisibility(View.VISIBLE);
        frameLightsList.setVisibility(View.VISIBLE);
        padLightsLayersLayout.setVisibility(View.GONE);
        colorsLeftPanelLayout.setVisibility(View.VISIBLE);
        colorsGridLayout.setVisibility(View.VISIBLE);
        colorsHexLayout.setVisibility(View.GONE);

        ((BaseAdapter) frameLightsList.getAdapter()).notifyDataSetChanged();
        currentLedsFramesLayout = led_frames;
        led_frames.removeAllViews();
        if(currentFrames != null){
            updateFramesLayout(led_frames, currentFrames);
        }
        led_frames_progress.setMax(led_frames.getColumnsCount());
        btColorVelocity.callOnClick();
    }

    public void configureInterfaceFramesGroups(){
        led_frames.setVisibility(View.GONE);
        led_frames_groups.setVisibility(View.VISIBLE);
        btFramesBack.setVisibility(View.GONE);
        frameLightsList.setVisibility(View.GONE);
        padLightsLayersLayout.setVisibility(View.VISIBLE);
        colorsGridLayout.setVisibility(View.GONE);
        colorsHexLayout.setVisibility(View.GONE);
        colorsLeftPanelLayout.setVisibility(View.GONE);

        currentLedsFramesLayout = led_frames_groups;
        currentFrames = null;
        currentFrame = null;
        if(currentFramesGroups != null){
            led_frames_groups.removeAllViews();
            updateFramesGroupsLayout(led_frames_groups, currentFramesGroups);
        }
        grid.clearLeds();
        //led_frames_progress.setMax(led_frames_groups.getColumnsCount());
    }

    public void updateFramesGroupsLayout(FramesLayout framesLayout, FramesGroups framesGroups){
        for(Frames frames : framesGroups.getList()){
            if(framesLayout.callOnTouch(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, META_STATE_RELOAD_FRAMES_TO_VIEW), frames.getYOffset(), frames.getXOffset()) &&
                    framesLayout.callOnTouch(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, META_STATE_RELOAD_FRAMES_TO_VIEW), frames.getYOffset(), frames.getXOffset()))
                currentFramesGroupView.setData(frames);
        }
        if(currentFramesGroupView != null) {
            currentFramesGroupView.getBackground().setTint(GlobalSettings.frames_group_color);
            currentFramesGroupView = null;
        }
    }
    public void updateFramesLayout(FramesLayout framesLayout, Frames frames){
        for(Frame frame : frames.getList()){
            if(
                    framesLayout.callOnTouch(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, META_STATE_RELOAD_FRAMES_TO_VIEW), frame.getYOffset(), frame.getXOffset()) &&
                    framesLayout.callOnTouch(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, META_STATE_RELOAD_FRAMES_TO_VIEW), frame.getYOffset(), frame.getXOffset()))
                currentFrameView.setData(frame);
        }
        if(currentFrameView != null) {
            currentFrameView.getBackground().setTint(GlobalSettings.frame_color);
            currentFrameView = null;
        }
    }

    // Metodos de interacao

    public View.OnTouchListener bidirecionalScroll(final View v) {
        if (v.getId() == led_frames_horizontal.getId()) {
            return new View.OnTouchListener() {
                float pivot_y = 0;
                int old_scroll_y = 0;
                boolean down = true;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            {
                                down = true;
                                return false;
                            }
                        case MotionEvent.ACTION_MOVE:
                            {
                                if (down == true) { // ACTION_DOWN nao funciona entao vamos
                                    // improvisar
                                    pivot_y = event.getY();
                                    old_scroll_y = led_frames_vertical.getScrollY();
                                    down = false;
                                }
                                float get_y = event.getY();
                                int to_y = (int) (old_scroll_y + (pivot_y - get_y));
                                led_frames_vertical.setScrollY(to_y);
                                return false;
                            }
                    }
                    return false;
                }
            };
        } else {
            return new View.OnTouchListener() {
                float pivot_x = 0;
                int old_scroll_x = 0;
                boolean down = true;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            {
                                down = true;
                                return false;
                            }
                        case MotionEvent.ACTION_MOVE:
                            {
                                if (down == true) { // ACTION_DOWN nao funciona entao vamos
                                    // improvisar
                                    pivot_x = event.getX();
                                    old_scroll_x = led_frames_horizontal.getScrollX();
                                    //  Toast.makeText(context, "X: " + old_scroll_x + "Y: " +
                                    // led_frames_horizontal.getScrollY(), 50).show();
                                    down = false;
                                }
                                float get_x = event.getX();
                                int to_x =
                                        (int)
                                                (led_frames_horizontal.getScrollX()
                                                        + (pivot_x - get_x));
                                led_frames_horizontal.setScrollX(to_x);
                                return false;
                            }
                    }
                    return false;
                }
            };
        }
    }
}
