package com.stephenfong.pepperchat.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.stephenfong.pepperchat.BuildConfig;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.databinding.ActivityMainBinding;
import com.stephenfong.pepperchat.menu.CallsFragment;
import com.stephenfong.pepperchat.menu.CameraFragment;
import com.stephenfong.pepperchat.menu.ChatsFragment;
import com.stephenfong.pepperchat.menu.StatusFragment;
import com.stephenfong.pepperchat.view.activities.contact.ContactsActivity;
import com.stephenfong.pepperchat.view.activities.settings.SettingsActivity;
import com.stephenfong.pepperchat.view.activities.status.AddStatusPicActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUpWithViewPager(binding.viewPager);
        // setupWithViewPager is a built-in method for TabLayout, setting up this TabLayout with a ViewPager
        // The TabLayout will be automatically populated from the PagerAdapter's page titles. By doing that,
        // when the user tabs on the tab, the appropriate fragment will be shown in the ViewPager
        // TabLayout provides a horizontal layout to display tabs.
        // Without this line you can still swipe left/right to see all the pages, just cannot tab on the
        // tab to switch pages, because you won't see the tabs
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        View camera = LayoutInflater.from(this).inflate(R.layout.custom_camera_tab, null);
        try {
            // reset the view inside the tab
            binding.tabLayout.getTabAt(0).setCustomView(camera);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Display CHATS when the activity starts
        binding.viewPager.setCurrentItem(1);

        // This method sets the toolbar as the app bar for the activity
        setSupportActionBar(binding.toolbar);

        // change the fab icon when the page is changed
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeFabIcon(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // initialize the contact button
        binding.fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
            }
        });


    }

    private void setUpWithViewPager(ViewPager viewPager) {
        // An inner class defined in MainActivity
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(new CameraFragment(), "");
        sectionsPagerAdapter.addFragment(new ChatsFragment(), "Chats");
        sectionsPagerAdapter.addFragment(new StatusFragment(), "Status");
        sectionsPagerAdapter.addFragment(new CallsFragment(), "Calls");
        // We need 3 fragments
        viewPager.setAdapter(sectionsPagerAdapter);
    }

    private void changeFabIcon(final int index) {
        binding.fabAction.hide();
        binding.btnAddStatus.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (index) {
                    case 0:
                        binding.fabAction.hide(); break;
                    case 1:

                        binding.fabAction.show();
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_baseline_chat_24));
                        break;
                    case 2:
                        binding.fabAction.show();
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_baseline_camera_alt_24));
                        binding.btnAddStatus.setVisibility(View.VISIBLE);
                        break;
                    case 3: binding.fabAction.show(); binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_baseline_call_24)); break;

                }
            }
        }, 400);

        performOnClick(index);
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }

    // Codes related to the menu at the top bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater is used to instantiate menu XML files into Menu objects.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search: Toast.makeText(this, "Action Search", Toast.LENGTH_SHORT).show(); break;
            case R.id.action_new_group: Toast.makeText(this, "Action New Group", Toast.LENGTH_SHORT).show(); break;
            case R.id.action_new_broadcast: Toast.makeText(this, "Action New Broadcast", Toast.LENGTH_SHORT).show(); break;
            case R.id.action_pc_web: Toast.makeText(this, "Action PC Web", Toast.LENGTH_SHORT).show(); break;
            case R.id.action_starred_message: Toast.makeText(this, "Action Starred Message", Toast.LENGTH_SHORT).show(); break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performOnClick(int index) {
        binding.fabAction.setOnClickListener((v) -> {
            if (index == 1) {
                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
            } else if (index == 2) {
//                Toast.makeText(getApplicationContext(), "Camera", Toast.LENGTH_SHORT).show();
                checkCameraPermission();

            } else {
                Toast.makeText(getApplicationContext(), "Call", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnAddStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Add status...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    8000);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    9000);
        } else {
            openCamera();
        }
    }

    public static Uri imageUri = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5000
                && resultCode == RESULT_OK) {
            //uploadToFirebase();
            if (imageUri != null) {
                startActivity(new Intent(MainActivity.this, AddStatusPicActivity.class)
                .putExtra("image", imageUri));
            }
        }
    }

    private String getFileExtension(Uri uri) {
        // The Content Resolver is the single, global instance in your application that provides access
        // to your (and other applications') content providers. The Content Resolver behaves exactly as
        // its name implies: it accepts requests from clients, and resolves these requests by directing
        // them to the content provider with a distinct authority.
        ContentResolver contentResolver = getContentResolver();
        // Two-way map that maps MIME-types to file extensions and vice versa.
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}