package com.xayup.multipad.pads.Render.skin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import com.xayup.multipad.global.Vars;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.toolpad.BuildConfig;
import com.xayup.toolpad.R;
import com.xayup.utils.Files;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkinManager {
    //From storage (JSON file)
    public static final String JSON_SKIN_NAME = "skin_name";
    public static final String JSON_SKIN_AUTHOR = "skin_author";
    public static final String JSON_SKIN_VERSION = "skin_version";
    public static final String JSON_SKIN_PATH = "skin_path";

    public interface SkinInfo {
        byte name = 0;
        byte author = 1;
        byte version = 2;
        byte package_name = 3;
    }

    public static List<JSONObject> listFromStorage(){
        File skins_path = new File(Vars.MULTIPAD_SKINS_PATH);
        if(!skins_path.exists()) if(!skins_path.mkdirs()) return null;
        List<JSONObject> skins_from_storage = new ArrayList<>();
        for(File folder : skins_path.listFiles()){
            File skin_info = new File(folder, "skin_info.json");
            if(skin_info.exists()){
                try { skins_from_storage.add(Files.readJson(skin_info).put(JSON_SKIN_PATH, skin_info.getParent()));
                } catch (IOException ignored){} catch (JSONException e) { throw new RuntimeException(e); }
            }
        }
        return skins_from_storage;
    }

    public static List<String[]> listFromApps(PackageManager pm){
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<String[]> skins_from_packages = new ArrayList<>();
        for (PackageInfo pi : packages) {
            if (pi.packageName.contains("com.kimjisub.launchpad.theme.")) {
                try{
                    Resources res = pm.getResourcesForApplication(pi.packageName);
                    String[] info = new String[4];
                    info[SkinInfo.name] = pi.applicationInfo.name;
                    info[SkinInfo.version] = pi.versionName;
                    info[SkinInfo.package_name] = pi.packageName;
                    info[SkinInfo.author] = res.getString(res.getIdentifier("theme_author", "string", pi.packageName));
                    skins_from_packages.add(info);
                } catch (PackageManager.NameNotFoundException nnfe){
                    Log.e("Skin package error", pi.packageName + ": " + nnfe.toString());
                }
            }
        }
        return skins_from_packages;
    }

    public static BaseAdapter getAdapterSkinsFromStorage(Context context){
        return new BaseAdapter() {
            final List<JSONObject> skins = listFromStorage();
            @Override
            public int getCount() { return skins.size(); }
            @Override
            public Object getItem(int i) { return skins.get(i); }
            @Override
            public long getItemId(int i) { return i; }
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view == null) view = LayoutInflater.from(context).inflate(R.layout.skinstheme_layout, viewGroup, false);
                try {
                    ((TextView) view.findViewById(R.id.skinthemeName)).setText(skins.get(i).getString(JSON_SKIN_NAME));
                    ((TextView) view.findViewById(R.id.skinthemeVersion)).setText(skins.get(i).getString(JSON_SKIN_VERSION));
                    try(FileInputStream img_file = new FileInputStream(skins.get(i).getString(JSON_SKIN_PATH) + "/theme_ic.png")){
                        ((ImageView) view.findViewById(R.id.skinthemelogo)).setImageBitmap(BitmapFactory.decodeStream(img_file));
                    } catch (IOException ignored){}
                } catch (JSONException ignored) {}
                return view;
            }
        };
    }

    public static BaseAdapter getAdapterSkinsFromApps(Context context){
        return new BaseAdapter() {
            final List<String[]> skins = listFromApps(context.getPackageManager());
            @Override
            public int getCount() { return skins.size(); }
            @Override
            public Object getItem(int i) { return skins.get(i); }
            @Override
            public long getItemId(int i) { return i; }
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view == null) view = LayoutInflater.from(context).inflate(R.layout.skinstheme_layout, viewGroup, false);
                ((TextView) view.findViewById(R.id.skinthemeName)).setText(skins.get(i)[SkinInfo.name]);
                ((TextView) view.findViewById(R.id.skinthemeVersion)).setText(skins.get(i)[SkinInfo.version]);
                try {
                    Resources res = context.getPackageManager().getResourcesForApplication(skins.get(i)[SkinInfo.package_name]);
                    ((ImageView) view.findViewById(R.id.skinthemelogo)).setImageDrawable(ResourcesCompat.getDrawable(res, res.getIdentifier("theme_ic", "drawable", skins.get(i)[SkinInfo.package_name]), null));
                } catch (PackageManager.NameNotFoundException ignored){}
                return view;
            }
        };
    }

    public static class SkinResources {
        public final Drawable PHANTOM, PHANTOM_, CHAINLED, BTN, BTN_, CHAIN, CHAIN_, CHAIN__, PLAYBG, CUSTOMLOGO;
        public SkinResources(
                Drawable phantom, Drawable phantom_, Drawable chainled, Drawable btn, Drawable btn_,
                Drawable chain, Drawable chain_, Drawable chain__, Drawable playbg, Drawable customlogo
        ){
            PHANTOM = phantom; PHANTOM_ = phantom_; CHAINLED = chainled; BTN = btn; BTN_ = btn_;
            CHAIN = chain; CHAIN_ = chain_; CHAIN__ = chain__; PLAYBG = playbg; CUSTOMLOGO = customlogo;
        }
    }
    public interface OnSkinResourceLoaded{
        void resourceLoadedBasic(View pad, MakePads.ChildInfo padInfo, SkinResources resources);
        void resourceLoadedAdvanced(View pad, MakePads.ChildInfo padInfo, Drawable resource, SkinResources defaultResources);

        /**
         * Apply background and others
         * @param defaultResources .
         */
        void finishResourceLoaded(SkinResources defaultResources);
    }

    public static boolean loadSkinResources(Context context, Object skin_info, MakePads.Pads mPads, OnSkinResourceLoaded resourceLoaded) throws PackageManager.NameNotFoundException, JSONException, IOException {
        String package_or_path;
        SkinResources sResources = null;
        if((skin_info instanceof String && ((package_or_path = (String) skin_info).startsWith("com.kimjisub.launchpad.theme.") || package_or_path.equals(BuildConfig.APPLICATION_ID))) ||
                skin_info instanceof String[] && !(package_or_path = ((String[]) skin_info)[SkinInfo.package_name]).isEmpty()){
            try {
                Resources res = context.getPackageManager().getResourcesForApplication(package_or_path);
                Drawable CHAINLED = null, CHAIN = null, CHAIN_ = null, CHAIN__ = null, PLAYBG = null, CUSTOMLOGO = null;
                int tmpid;

                if((tmpid = res.getIdentifier("chainled", "drawable", package_or_path)) > 0) CHAINLED = res.getDrawable(tmpid, null);
                else if((tmpid = res.getIdentifier("chain", "drawable", package_or_path)) > 0){
                    CHAIN = ResourcesCompat.getDrawable(res, tmpid, null);
                    CHAIN_ = ResourcesCompat.getDrawable(res, res.getIdentifier("chain_", "drawable", package_or_path), null);
                    CHAIN__ = ResourcesCompat.getDrawable(res, res.getIdentifier("chain__", "drawable", package_or_path), null);
                }
                if((tmpid = res.getIdentifier("applogo", "drawable", package_or_path)) != 0
                        || (tmpid = res.getIdentifier("logo", "drawable", package_or_path)) != 0
                        || (tmpid = res.getIdentifier("custom_logo", "drawable", package_or_path)) != 0
                        || (tmpid = res.getIdentifier("theme_ic", "drawable", package_or_path)) != 0)
                    CUSTOMLOGO = ResourcesCompat.getDrawable(res, tmpid, null);
                else CUSTOMLOGO = AppCompatResources.getDrawable(context,
                        context.getResources().getIdentifier("customlogo", "drawable", context.getPackageName()));
                if((tmpid = res.getIdentifier("playbg_pro", "drawable", package_or_path)) != 0 || (tmpid = res.getIdentifier("playbg", "drawable", package_or_path)) != 0)
                    PLAYBG = ResourcesCompat.getDrawable(res, tmpid, null);
                sResources = new SkinResources(
                        ResourcesCompat.getDrawable(res, res.getIdentifier("phantom", "drawable", package_or_path), null),
                        ResourcesCompat.getDrawable(res, res.getIdentifier("phantom_", "drawable", package_or_path), null),
                        CHAINLED, ResourcesCompat.getDrawable(res, res.getIdentifier("btn", "drawable", package_or_path), null),
                        ResourcesCompat.getDrawable(res, res.getIdentifier("btn_", "drawable", package_or_path), null),
                        CHAIN, CHAIN_, CHAIN__, PLAYBG, CUSTOMLOGO
                );
                for(byte row = 0; row < mPads.getRows(); row++)
                    for(byte colum = 0; colum < mPads.getColumns(); colum++)
                        resourceLoaded.resourceLoadedBasic(mPads.getPadView(row, colum), mPads.getPadInfo(row, colum), sResources);
            } catch (PackageManager.NameNotFoundException nnfe){
                Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_from_app), Toast.LENGTH_SHORT).show();
            }
        } else if((skin_info instanceof String && new File((package_or_path = (String) skin_info)).isDirectory()) ||
            skin_info instanceof JSONObject && !(package_or_path = ((JSONObject) skin_info).getString(JSON_SKIN_PATH)).isEmpty()){
            List<String> files = Arrays.asList(new File(package_or_path).list());
            JSONObject json = Files.readJson(new File(package_or_path, "skin_info.json"));

            Drawable CHAINLED = null, CHAIN = null, CHAIN_ = null, CHAIN__ = null, PLAYBG = null, CUSTOMLOGO = null;

            if (files.contains("chainled.xml")) {
                CHAINLED = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/chainled.xml")));
            } else if (files.contains("chainled.png")) {
                CHAINLED = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/chainled.png")));
            } else if (files.contains("chain.png")) {
                CHAIN = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/chain.png")));
                CHAIN_ = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/chain_.png")));
                CHAIN__ = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/chain__.png")));
            }
            if (files.contains("applogo.png"))
                CUSTOMLOGO = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/applogo.png")));
            else if (files.contains("logo.png"))
                CUSTOMLOGO = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/logo.png")));
            else if (files.contains("custom_logo.png"))
                CUSTOMLOGO = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/custom_logo.png")));
            else if (files.contains("theme_ic.png"))
                CUSTOMLOGO = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/theme_ic.png")));
            else
                CUSTOMLOGO = AppCompatResources.getDrawable(context, context.getResources().getIdentifier("customlogo", "drawable", context.getPackageName()));
            PLAYBG = (files.contains("playbg_pro.png")) ? new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/playbg_pro.png"))) : new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/playbg.png")));

            sResources = new SkinResources(
                    new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/phantom").concat((files.contains("phantom.xml")) ? ".xml" : ".png"))),
                            new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/phantom_").concat((files.contains("phantom_.xml")) ? ".xml" : ".png"))),
                    CHAINLED, new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/btn").concat((files.contains("btn.xml")) ? ".xml" : ".png"))),
                                            new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/btn_").concat((files.contains("btn_.xml")) ? ".xml" : ".png"))),
                    CHAIN, CHAIN_, CHAIN__, PLAYBG, CUSTOMLOGO
            );
            if(json.has("skin_type") && json.getString("skin_type").equalsIgnoreCase("advanced")){
                if(!json.has("skin_pads")) return false;
                else {
                    JSONObject jsonObj = json.getJSONObject("skin_pads");
                    for(byte row = 0; row < mPads.getRows(); row++)
                        for(byte colum = 0; colum < mPads.getColumns(); colum++)
                            resourceLoaded.resourceLoadedAdvanced(mPads.getPadView(row, colum), mPads.getPadInfo(row, colum),
                                        (jsonObj.has(row+","+colum)) ? new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(package_or_path.concat("/"+jsonObj.getString(row+","+colum))))
                                        : null, sResources);
                }
            } else
                for(byte row = 0; row < mPads.getRows(); row++)
                    for(byte colum = 0; colum < mPads.getColumns(); colum++)
                        resourceLoaded.resourceLoadedBasic(mPads.getPadView(row, colum), mPads.getPadInfo(row, colum), sResources);

            resourceLoaded.finishResourceLoaded(sResources);
        } else return false;
        resourceLoaded.finishResourceLoaded(sResources);
        return true;
    }

    public static void updateSkin(Context context, MakePads.Pads mPads, String package_or_path) {
        Log.v("Try apply skin", package_or_path);
        try {
            if(!SkinManager.loadSkinResources(context, package_or_path, mPads, new OnSkinResourceLoaded() {
                @Override
                public void resourceLoadedBasic(View pad, MakePads.ChildInfo padInfo, SkinResources skinResources) {
                    //Log.v("updateSkin", padInfo.getRow() + " " + padInfo.getColum());
                    if(padInfo == null) return;
                    if (padInfo.getType() == MakePads.PadType.CHAIN) {
                        ImageView btn_ = ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_));
                        if (skinResources.CHAIN != null) {
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.CHAIN);
                            btn_.setImageDrawable(skinResources.CHAIN_);
                            btn_.setBackground(null);
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.CHAIN_LED)).setImageDrawable(null);
                            pad.findViewById(MakePads.PadInfo.PadLayerType.LED).setVisibility(View.INVISIBLE);
                        } else {
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.BTN);
                            btn_.setImageDrawable(skinResources.BTN_);
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.CHAIN_LED)).setImageDrawable(skinResources.CHAINLED);
                            pad.findViewById(MakePads.PadInfo.PadLayerType.LED).setVisibility(View.VISIBLE);
                        }
                    } else if (padInfo.getType() == MakePads.PadType.NONE) {
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM)).setImageDrawable(null);
                        pad.requestLayout();
                    } else {
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.BTN);
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_)).setImageDrawable(skinResources.BTN_);
                        if (padInfo.getType() == MakePads.PadType.PAD) {
                            try {
                                ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM)).setImageDrawable(skinResources.PHANTOM);
                            } catch (NullPointerException n) {
                                ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM_)).setImageDrawable(skinResources.PHANTOM_);
                            }
                        } else if (padInfo.getType() == MakePads.PadType.PAD_LOGO) {
                            ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.LOGO)).setImageDrawable(skinResources.CUSTOMLOGO);
                        }
                    }
                    pad.requestLayout();
                    mPads.defaultRotationSetting();
                }

                @Override
                public void resourceLoadedAdvanced(View pad, MakePads.ChildInfo padInfo, Drawable resource, SkinManager.SkinResources skinResources) {
                    if (padInfo == null) return;
                    byte viewId = 0;
                    if (resource != null) {
                        pad.setRotation(0);
                        pad.setScaleX(1);
                        pad.setScaleY(1);
                    }
                    if (padInfo.getType() == MakePads.PadType.CHAIN) {
                        viewId = MakePads.PadInfo.PadLayerType.CHAIN_LED;
                        if (resource == null)
                            if ((resource = skinResources.CHAINLED) == null)
                                resource = AppCompatResources.getDrawable(context, R.drawable.chainled);
                    } else if (padInfo.getType() == MakePads.PadType.NONE) {
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM)).setImageDrawable(resource);
                        pad.requestLayout();
                        return;
                    } else {
                        if (padInfo.getType() == MakePads.PadType.PAD)
                            if (pad.findViewById(MakePads.PadInfo.PadLayerType.PHANTOM) != null) {
                                viewId = MakePads.PadInfo.PadLayerType.PHANTOM;
                                if (resource == null)
                                    if ((resource = skinResources.PHANTOM) == null)
                                        resource = AppCompatResources.getDrawable(context, R.drawable.phantom);
                            } else {
                                viewId = MakePads.PadInfo.PadLayerType.PHANTOM_;
                                if (resource == null)
                                    if ((resource = skinResources.PHANTOM_) == null)
                                        resource = AppCompatResources.getDrawable(context, R.drawable.phantom_);
                            }
                        else if (padInfo.getType() == MakePads.PadType.PAD_LOGO) {
                            viewId = MakePads.PadInfo.PadLayerType.LOGO;
                            if (resource == null)
                                if ((resource = skinResources.CUSTOMLOGO) == null)
                                    resource = AppCompatResources.getDrawable(context, R.drawable.customlogo);
                            //((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.LOGO_BTN)).setImageDrawable(skinResources.BTN);
                        } else return;
                        ((ImageView) pad.findViewById(viewId)).setImageDrawable(resource);
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN)).setImageDrawable(skinResources.BTN);
                        ((ImageView) pad.findViewById(MakePads.PadInfo.PadLayerType.BTN_)).setImageDrawable(skinResources.BTN_);
                        pad.requestLayout();
                    }
                }

                @Override
                public void finishResourceLoaded (SkinResources skinResources){
                    //if(skinResources != null) playBgimg.setImageDrawable(skinResources.PLAYBG);
                    mPads.getRoot().setBackground(AppCompatResources.getDrawable(context, R.drawable.playbg));
                    mPads.getRoot().requestLayout();
                }
            })){Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_skin), Toast.LENGTH_SHORT).show();}
        } catch (JSONException | IOException je){
            Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_from_storage), Toast.LENGTH_SHORT).show();
            Log.e("JSON or IO exception", je.toString());
        } catch (PackageManager.NameNotFoundException nnfe){
            Toast.makeText(context, context.getString(R.string.skin_failed_get_resources_from_app), Toast.LENGTH_SHORT).show();
            Log.e("NameNotFoundException", nnfe.toString());

        }
    }
}
