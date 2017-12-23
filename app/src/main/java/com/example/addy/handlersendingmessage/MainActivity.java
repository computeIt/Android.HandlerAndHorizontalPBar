package com.example.addy.handlersendingmessage;

/*отправка расширенных сообщений с помощью Handler
* пример с прогрессбаром показывающим степень закачки файлов*/

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = "myLog";
    final int STATUS_NONE = 0;
    final int STATUS_CONNECTING = 1;
    final int STATUS_CONNECTED = 2;
    final int STATUS_DOWNLOAD_START = 3;
    final int STATUS_DOWNLOAD_FILE = 4;
    final int STATUS_DOWNLOAD_END = 5;
    final int STATUS_DOWNLOAD_NONE = 6;

    Handler handler;
    TextView textView;
    ProgressBar progressBar;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        button = findViewById(R.id.btnConnect);
        progressBar = findViewById(R.id.progressBar);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case STATUS_NONE:
                        button.setEnabled(true);
                        textView.setText("not connected");
                        progressBar.setVisibility(View.GONE);
                        break;
                    case STATUS_CONNECTING:
                        button.setEnabled(false);
                        textView.setText("connecting");
                        break;
                    case STATUS_CONNECTED:
                        textView.setText("connected");
                        break;
                    case STATUS_DOWNLOAD_START:
                        textView.setText("start download " + msg.arg1 + " files");
                        progressBar.setMax(msg.arg1);
                        progressBar.setProgress(0);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case STATUS_DOWNLOAD_FILE:
                        textView.setText("downloading. left " + msg.arg2 + " files");
                        progressBar.setProgress(msg.arg1);
                        saveFile((byte[])msg.obj);
                        break;
                    case STATUS_DOWNLOAD_END:
                        textView.setText("download complete!");

                        break;
                    case STATUS_DOWNLOAD_NONE:
                        textView.setText("nothing to download");
                        break;
                    default:
                        break;
                }
            }
        };
        handler.sendEmptyMessage(STATUS_NONE);
    }



    public void onclick(View v){
        Thread thread = new Thread(new Runnable() {
            Message msg;
            byte[] file;
            Random rand = new Random();

            @Override
            public void run() {
                try {
                    //после нажатия кнопки якобы устанавливаем соединение в течение 2 секунд
                    handler.sendEmptyMessage(STATUS_CONNECTING);
                    TimeUnit.SECONDS.sleep(1);

                    //подключились и якобы чтото делаем 3 секунды
                    handler.sendEmptyMessage(STATUS_CONNECTED);
                    TimeUnit.SECONDS.sleep(1);
                    int filesCount = rand.nextInt(5)+5;//генерим случайное кол-во файлов в пределах 5

                    if(filesCount == 0){//если сгенерили 0 то сообщаем что файлов для загрузки нет
                        handler.sendEmptyMessage(STATUS_DOWNLOAD_NONE);
                        TimeUnit.MILLISECONDS.sleep(1500);
                        handler.sendEmptyMessage(STATUS_NONE);
                        return;
                    }

                    //начинается загрузка
                    //создаем сообщение о кол-ве файлов
                    msg = handler.obtainMessage(STATUS_DOWNLOAD_START, filesCount, 0);

                    //отправляем его
                    handler.sendMessage(msg);

                    for (int i = 1; i <= filesCount; i++) {
                        //загружается файл
                        file = downloadFile();

                        //создаем сообщение о порядковом номере файла, кол-ве оставшихся файлов, и самим файлом
                        msg = handler.obtainMessage(STATUS_DOWNLOAD_FILE, i, filesCount-i, file);

                        //отправляем
                        handler.sendMessage(msg);
                    }

                    //загрузка завершена
                    handler.sendEmptyMessage(STATUS_DOWNLOAD_END);

                    //разрываем соединение
                    handler.sendEmptyMessage(STATUS_NONE);

                    //отключаемся
                    TimeUnit.MILLISECONDS.sleep(1500);
                    handler.sendEmptyMessage(STATUS_NONE);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private byte[] downloadFile() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        return new byte[1024];
    }

    private void saveFile(byte[] obj) {
    }
}
