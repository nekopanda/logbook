/**
 * 
 */
package logbook.gui;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.TestData;
import logbook.data.context.GlobalContext;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Nekopanda
 * 
 * テストデータを食べさせる
 * JSON保存先のパスをセットしてリセットを押すと準備ができます。
 * マウスホイールを回すとJSONファイルを食べさせます。
 * 各種ボタンで一気に食べさせることもできます。
 * 
 * ！注意！
 * 動作テスト用なのでログ出力も行います。
 * 古いログが追加されていくので本番使用環境では使わないでください。
 * 食べさせたログは艦これ統計データベースへの送信は行いません。
 * アクセスしたURLの情報が必要な建造や開発、艦の入れ替えなどはテストできません。
 */
public class TestDataFeeder extends WindowBase {

    private Text filepathText;
    private Label statusLabel;
    private String[] fileList;
    private int currentIndex;

    public TestDataFeeder(WindowBase parent) {
        this.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
        this.getShell().setText("JSONを食べさせます");
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            // リロードして表示
            this.setVisible(true);
            return;
        }

        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        Shell shell = this.getShell();
        shell.setLayout(new GridLayout(2, false));

        Label desc = new Label(shell, SWT.NONE);
        desc.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        desc.setText("使い方はソースコード(TestDataFeeder.java)を見てください");

        this.filepathText = new Text(shell, SWT.BORDER);
        GridData gd = new GridData(
                GridData.FILL_HORIZONTAL, SWT.CENTER, true, false, 1, 1);
        gd.widthHint = SwtUtils.DPIAwareWidth(250);
        this.filepathText.setLayoutData(gd);
        this.filepathText.setText("<よく分からないときは使わないでね>");

        Button btn = new Button(shell, SWT.NONE);
        btn.setText("リセット");
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TestDataFeeder.this.resetFilePath();
            }
        });

        Button btn1 = new Button(shell, SWT.NONE);
        btn1.setText("マスターデータまで進める");
        btn1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TestDataFeeder.this.nextUntil(DataType.START2);
            }
        });

        Button btn2 = new Button(shell, SWT.NONE);
        btn2.setText("次の出撃まで進める");
        btn2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TestDataFeeder.this.nextUntil(DataType.START);
            }
        });

        Button btn3 = new Button(shell, SWT.NONE);
        btn3.setText("次の連合艦隊戦まで進める");
        btn3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TestDataFeeder.this.nextUntil(DataType.COMBINED_BATTLE_WATER);
            }
        });

        Button btn4 = new Button(shell, SWT.NONE);
        btn4.setText("全て読み込む");
        btn4.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ApplicationMain.disableUpdate = true;
                TestDataFeeder.this.readAll();
            }
        });

        this.statusLabel = new Label(shell, SWT.NONE);
        this.statusLabel.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL, SWT.CENTER, true, false, 2, 1));

        shell.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                TestDataFeeder.this.feedJson();
            }
        });

        shell.pack();
    }

    private void updateLabel() {
        this.statusLabel.setText(String.valueOf(this.currentIndex) + "/" + this.fileList.length);
        this.getShell().layout();
    }

    private void resetFilePath() {
        File dir = new File(this.filepathText.getText());
        File[] files = dir.listFiles();
        this.fileList = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            this.fileList[i] = files[i].getAbsolutePath();
        }
        Arrays.sort(this.fileList);
        this.currentIndex = 0;

        this.updateLabel();
    }

    private void readAll() {
        if (this.fileList == null) {
            return;
        }
        // まずは100個
        int end = Math.min(this.currentIndex + 1000, this.fileList.length);
        for (; this.currentIndex < end;) {
            String filepath = this.fileList[this.currentIndex++];
            try {
                Data data = new TestData(filepath);
                GlobalContext.updateContext(data);
            } catch (ParseException | IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        this.updateLabel();
        // 末尾再帰？？
        if (this.currentIndex < this.fileList.length) {
            this.getShell().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!TestDataFeeder.this.getShell().isDisposed()) {
                        TestDataFeeder.this.readAll();
                    }
                }
            });
        }
        else {
            ApplicationMain.disableUpdate = false;
        }
    }

    private void nextUntil(DataType type) {
        if (this.fileList == null)
            return;
        for (; this.currentIndex < this.fileList.length;) {
            String filepath = this.fileList[this.currentIndex++];
            try {
                Data data = new TestData(filepath);
                GlobalContext.updateContext(data);
                this.updateLabel();
                if (data.getDataType() == type) {
                    break;
                }
            } catch (ParseException | IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void feedJson() {
        if ((this.fileList != null) && (this.currentIndex < this.fileList.length)) {
            String filepath = this.fileList[this.currentIndex++];
            try {
                Data data = new TestData(filepath);
                GlobalContext.updateContext(data);
                this.updateLabel();
            } catch (ParseException | IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

}
