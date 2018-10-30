package com.iternull.secureif;

/*
 *         ___
 *         ',_`""\        .---,
 *            \   :-""``/`    |
 *             `;'     //`\   /
 *             /   __     |   ('.
 *            |_ ./O)\     \  `) \
 *           _/-.    `      `"`  |`-.
 *       .-=; `                  /   `-.
 *      /o o \   ,_,           .        '.
 *      L._._;_.-'           .            `'-.
 *        `'-.`             '                 `'-.
 *            `.         '                        `-._
 *              '-._. -'                              '.
 *                 \                                    `\
 *                  |                                     \
 *                  |    |                                 ;   _.
 *                  \    |           |                     |-.((
 *                   ;.  \           /    /                |-.`\)
 *                   | '. ;         /    |                 |(_) )
 *                   |   \ \       /`    |                 ;'--'
 *                    \   '.\    /`      |                /
 *                     |   /`|  ;        \               /
 *                     |  |  |  |-._      '.           .'
 *                     /  |  |  |__.`'---"_;'-.     .-'
 *                    //__/  /  |    .-'``     _.-'`
 *                  jgs     //__/   //___.--''`
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static java.lang.Runtime.getRuntime;

public class MainActivity extends AppCompatActivity {

    private Switch SwitchBoot, SwitchCharge, SwitchConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SwitchBoot = findViewById(R.id.switch_boot);
        SwitchCharge = findViewById(R.id.switch_usbcharge);
        SwitchConnection = findViewById(R.id.switch_usbdata);
        if (ckMagisk()) {
            SwitchBoot.setChecked(ckScript());
        } else {
            SwitchBoot.setEnabled(ckMagisk());
            Toast.makeText(MainActivity.this, R.string.need_magisk, Toast.LENGTH_SHORT).show();
        }
        SwitchCharge.setChecked(getValue(new String[]{"cat", "/sys/class/power_supply/battery/input_suspend"}, "1"));
        SwitchConnection.setChecked(getValue(new String[]{"getprop", "persist.sys.usb.config"}, "none"));

        SwitchBoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = SwitchBoot.isChecked();
                if(check) {
                    cpScript();
                    Toast.makeText(MainActivity.this, R.string.boot_enabled, Toast.LENGTH_SHORT).show();
                }
                else {
                    rmScript();
                    Toast.makeText(MainActivity.this, R.string.boot_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        });

        SwitchCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = SwitchCharge.isChecked();
                if(!check) {
                    try {
                        Process exec1 = getRuntime().exec(new String[]{"su", "-c", "echo 1 > /sys/class/power_supply/battery/input_suspend"});
                        exec1.waitFor();
                        Toast.makeText(MainActivity.this, R.string.usb_charging_is_disabled, Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        Process exec1 = getRuntime().exec(new String[]{"su", "-c", "echo 0 > /sys/class/power_supply/battery/input_suspend"});
                        exec1.waitFor();
                        Toast.makeText(MainActivity.this, R.string.usb_charging_is_enabled, Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        SwitchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = SwitchConnection.isChecked();
                if(!check) try {
                    Process exec1 = getRuntime().exec(new String[]{"su", "-c", "setprop persist.sys.usb.config none"});
                    exec1.waitFor();
                    Process exec2 = getRuntime().exec(new String[]{"su", "-c", "setprop sys.usb.config none"});
                    exec2.waitFor();
                    Process exec3 = getRuntime().exec(new String[]{"su", "-c", "setprop sys.usb.configfs 0"});
                    exec3.waitFor();
                    //Brevent/Shizuku Server/Storage Redirect root method compatible
                    Process exec4 = getRuntime().exec(new String[]{"su", "-c", "start adbd"});
                    exec4.waitFor();
                    Toast.makeText(MainActivity.this, R.string.usb_port_is_disabled, Toast.LENGTH_SHORT).show();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                else try {
                    Process exec1 = getRuntime().exec(new String[]{"su", "-c", "setprop persist.sys.usb.config 'mtp,mass_storage,adb'"});
                    exec1.waitFor();
                    Process exec2 = getRuntime().exec(new String[]{"su", "-c", "setprop sys.usb.config 'mtp,mass_storage,adb'"});
                    exec2.waitFor();
                    Process exec3 = getRuntime().exec(new String[]{"su", "-c", "setprop sys.usb.configfs 1"});
                    exec3.waitFor();
                    Toast.makeText(MainActivity.this, R.string.usb_port_is_enabled, Toast.LENGTH_SHORT).show();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onShowHelpDialog() {
        CharSequence styledText = Html.fromHtml(getString(R.string.text_help));
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(R.string.action_help)
                .setMessage(styledText)
                .setPositiveButton(R.string.action_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing.
                            }
                        }).create();
        ad.show();
        // Make links clickable.
        ((TextView) Objects.requireNonNull(ad.findViewById(android.R.id.message))).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    private void onShowAboutDialog() {
        CharSequence styledText = Html.fromHtml(getString(R.string.text_about));
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(R.string.action_about)
                .setMessage(styledText)
                .setPositiveButton(R.string.action_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing.
                            }
                        }).create();
        ad.show();
        // Make links clickable.
        ((TextView) Objects.requireNonNull(ad.findViewById(android.R.id.message))).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    public boolean getValue(String[] command, String def) {
        String value = null;
        try {
            Process process = getRuntime().exec(command);
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            value = bufferedReader.readLine();
            process.waitFor();
            inputStream.close();
            bufferedReader.close();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        assert value != null;
        return !value.equals(def);
    }

    public boolean ckMagisk() {
        int result = 1;
        try {
            Process exec = getRuntime().exec(new String[]{"which", "magisk"});
            result = exec.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result == 0;
    }

    public boolean ckScript() {
        int result = 1;
        try {
            Process exec = getRuntime().exec(new String[]{"su", "-c", "ls", "/sbin/.core/img/.core/service.d/secure-if.sh"});
            result = exec.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result == 0;
    }

    public void cpScript() {
        try {
            InputStream inputStream = getAssets().open("secure-if.sh");
            File file = new File(getFilesDir().getAbsolutePath() + File.separator + "secure-if.sh");
            if (!file.exists() || file.length() == 0) {
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len=inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                inputStream.close();
                fos.close();
            }
            new ProcessBuilder("su", "-c", "cp", String.valueOf(file), "/sbin/.core/img/.core/service.d/secure-if.sh").start().waitFor();
            new ProcessBuilder("su", "-c", "chmod", "0755", "/sbin/.core/img/.core/service.d/secure-if.sh").start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void rmScript() {
        try {
            new ProcessBuilder("su", "-c", "rm", "-f", "/sbin/.core/img/.core/service.d/secure-if.sh").start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            onShowHelpDialog();
            return true;
        } else if (id == R.id.action_about) {
            onShowAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
