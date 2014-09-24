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
 * テストデータを食べさせる
 */
public class TestDataFeeder extends WindowBase {

    private Text filepathText;
    private Label statusLabel;
    private String[] fileList;
    private int currentIndex;

    public TestDataFeeder(WindowBase parent) {
        this.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
        this.getShell().setText("テストデータを食べさせます");
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

        this.filepathText = new Text(shell, SWT.BORDER);
        GridData gd = new GridData(
                GridData.FILL_HORIZONTAL, SWT.CENTER, true, false, 1, 1);
        gd.widthHint = 250;
        this.filepathText.setLayoutData(gd);
        this.filepathText.setText("O:\\艦これJSON\\Now\\メイン");

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
                TestDataFeeder.this.nextUntil(DataType.COMBINED_AIR_BATTLE);
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

        for (; this.currentIndex < this.fileList.length;) {
            String filepath = this.fileList[this.currentIndex++];
            try {
                Data data = new TestData(filepath);
                GlobalContext.updateContext(data);
                if ((this.currentIndex % 64) == 0)
                    this.updateLabel();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }

        this.updateLabel();
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
            } catch (ParseException | IOException e) {
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
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
