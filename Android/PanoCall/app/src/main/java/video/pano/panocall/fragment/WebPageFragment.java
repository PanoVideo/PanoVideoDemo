package video.pano.panocall.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import video.pano.panocall.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class WebPageFragment extends Fragment {

    private String mWebLink = "https://pano.video/";

    public WebPageFragment(String webLink) {
        // Required empty public constructor
        if(!TextUtils.isEmpty(webLink)){
            mWebLink = webLink ;
        }
    }

    public void setWebLink(String link) {
        mWebLink = link;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_page, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView webView = view.findViewById(R.id.web_view);
        webView.setWebViewClient(new PanoWebViewClient());
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDisplayZoomControls(true);
        webView.loadUrl(mWebLink);
    }


    private static class PanoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
