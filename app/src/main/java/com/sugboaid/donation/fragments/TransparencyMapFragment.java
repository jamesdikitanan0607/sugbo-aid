package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.BarangayAdapter;
import com.sugboaid.donation.models.BarangayLocation;
import com.sugboaid.donation.viewmodels.TransparencyViewModel;
import com.sugboaid.donation.viewmodels.TransparencyViewModel;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;


import java.util.List;

/**
 * Barangay Map fragment showing interactive map with location markers
 * Displays barangay locations with donation information and family counts
 */
public class TransparencyMapFragment extends BaseFragment {

    private WebView mapWebView;
    private RecyclerView barangayListRecyclerView;
    private TextView totalBarangaysText;
    private TextView totalFamiliesText;
    private BarangayAdapter barangayAdapter;
    private TransparencyViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transparency_map, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransparencyViewModel.class);
    }

    @Override
    protected void initViews(View view) {
        mapWebView = view.findViewById(R.id.mapWebView);
        barangayListRecyclerView = view.findViewById(R.id.barangayListRecyclerView);
        totalBarangaysText = view.findViewById(R.id.totalBarangaysText);
        totalFamiliesText = view.findViewById(R.id.totalFamiliesText);

        // Setup RecyclerView (horizontal scrolling cards)
        barangayAdapter = new BarangayAdapter();
        barangayListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        barangayListRecyclerView.setHasFixedSize(true);
        barangayListRecyclerView.setAdapter(barangayAdapter);
        // Equal spacing between cards
        final int spacingPx = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
        barangayListRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
                outRect.top = 0;
                outRect.bottom = 0;
                outRect.left = position == 0 ? spacingPx : spacingPx / 2;
                outRect.right = position == itemCount - 1 ? spacingPx : spacingPx / 2;
            }
        });

        // Prevent parent (ViewPager2/ScrollView) from intercepting when horizontally scrolling the list
        barangayListRecyclerView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        // Initialize map
        if (mapWebView != null) {
            WebSettings webSettings = mapWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            
            // Helpful for debugging - allows seeing console logs in Logcat
            mapWebView.setWebChromeClient(new android.webkit.WebChromeClient());
            
            // Handle SSL errors and page loading
            mapWebView.setWebViewClient(new android.webkit.WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed(); // Ignore SSL errors for development
                }
            });

            // Set a standard User Agent
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile; rv:88.0) Gecko/88.0 Firefox/88.0");

            // Allow mixed content (HTTP content in HTTPS WebView) to ensure CDNs load
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            // Leaflet (OpenStreetMap) HTML
            String mapHtml = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.css\" />" +
                    "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.js\"></script>" +
                    "<style>" +
                    "  html, body { height: 100%; margin: 0; padding: 0; background: #ffffff; }" +
                    "  #map { height: 100%; width: 100%; }" +
                    "  #status { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #333; font-family: sans-serif; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "  <div id=\"status\">Initializing Map...</div>" +
                    "  <div id=\"map\"></div>" +
                    "<script>" +
                    "  var map;" +
                    "  var currentMarker = null;" +
                    "  " +
                    "  function initMap() {" +
                    "    var status = document.getElementById('status');" +
                    "    " +
                    "    if (typeof L === 'undefined') {" +
                    "      status.innerText = 'Leaflet failed to load. Check internet.';" +
                    "      return;" +
                    "    }" +
                    "    " +
                    "    var cebu = [10.3157, 123.8854];" +
                    "    try {" +
                    "        map = L.map('map', { zoomControl: false }).setView(cebu, 12);" +
                    "        " +
                    "        var iconUrl = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png';" +
                    "        var iconRetinaUrl = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png';" +
                    "        var shadowUrl = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png';" +
                    "        " +
                    "        var DefaultIcon = L.icon({" +
                    "            iconUrl: iconUrl," +
                    "            iconRetinaUrl: iconRetinaUrl," +
                    "            shadowUrl: shadowUrl," +
                    "            iconSize: [25, 41]," +
                    "            iconAnchor: [12, 41]," +
                    "            popupAnchor: [1, -34]," +
                    "            shadowSize: [41, 41]" +
                    "        });" +
                    "        L.Marker.prototype.options.icon = DefaultIcon;" +
                    "" +
                    "        L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {" +
                    "            attribution: '&copy; OpenStreetMap &copy; CARTO'," +
                    "            subdomains: 'abcd'," +
                    "            maxZoom: 20" +
                    "        }).addTo(map);" +
                    "        " +
                    "        status.style.display = 'none';" +
                    "    } catch (e) {" +
                    "        status.innerText = 'Map Error: ' + e.message;" +
                    "    }" +
                    "  }" +
                    "  " +
                    "  function moveToLocation(lat, lng) {" +
                    "    if (!map) return;" +
                    "    map.flyTo([lat, lng], 15, { animate: true, duration: 1.5 });" +
                    "    if (currentMarker) map.removeLayer(currentMarker);" +
                    "    currentMarker = L.marker([lat, lng]).addTo(map);" +
                    "  }" +
                    "  " +
                    "  // Execute immediately after body load\n" +
                    "  initMap();" +
                    "</script>" +
                    "</body>" +
                    "</html>";
            
            mapWebView.loadDataWithBaseURL("https://www.sugboaid.org", mapHtml, "text/html", "UTF-8", null);

            // Allow panning/zooming without parent scroll/viewpager intercept
            mapWebView.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false; // let the map handle the gesture
            });
        }
    }

    @Override
    protected void setupListeners() {
        // Set up barangay item click listener
        barangayAdapter.setOnBarangayClickListener(barangay -> {
            if (mapWebView != null) {
                // Bridge Native -> JS
                // Call the JavaScript function defined in the HTML above
                String jsCommand = String.format(java.util.Locale.US, "moveToLocation(%f, %f)", 
                        barangay.getLatitude(), barangay.getLongitude());
                
                mapWebView.evaluateJavascript(jsCommand, null);
            }
        });

        // Map marker click listener removed

    }

    @Override
    protected void observeData() {
        // Observe barangay data
        viewModel.getBarangayLocations().observe(getViewLifecycleOwner(), this::updateBarangayData);
        
        // Observe summary statistics
        viewModel.getTotalBarangays().observe(getViewLifecycleOwner(), total -> {
            if (totalBarangaysText != null) {
                totalBarangaysText.setText(String.valueOf(total));
            }
        });

        viewModel.getTotalFamiliesHelped().observe(getViewLifecycleOwner(), families -> {
            if (totalFamiliesText != null) {
                totalFamiliesText.setText(String.valueOf(families));
            }
        });
    }

    @Override
    protected void refreshData() {
        if (viewModel != null) {
            viewModel.refreshBarangayData();
        }
    }

    private void updateBarangayData(List<BarangayLocation> barangays) {
        if (barangayAdapter != null) {
            barangayAdapter.updateBarangays(barangays);
        }
        
        // Marker updates removed

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapWebView != null) {
            mapWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapWebView != null) {
            mapWebView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapWebView != null) {
            mapWebView.destroy();
        }
        
        // Clean up references
        mapWebView = null;
        barangayListRecyclerView = null;
        totalBarangaysText = null;
        totalFamiliesText = null;
        barangayAdapter = null;
    }
}