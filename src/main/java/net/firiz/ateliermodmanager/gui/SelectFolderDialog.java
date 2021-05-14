package net.firiz.ateliermodmanager.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;

import net.firiz.ateliermodmanager.file.FileManager;
import net.firiz.ateliermodmanager.file.pak.Pak;
import swing2swt.layout.BorderLayout;

public class SelectFolderDialog extends Dialog {

    private static final FileManager fileManager = FileManager.INSTANCE;
    protected Result result;
    protected Shell shlMod;

    private CLabel messageLabel;
    private Text modNameText;
    private Tree fileTree;
    private StackImageComposite imageStackComposite;

    private File file;

    public SelectFolderDialog(Shell parent, File file) {
        super(parent, SWT.NONE);
        this.file = file;
    }

    /**
     * Open the dialog.
     *
     * @return the result
     */
    public Result open() {
        createContents();
        shlMod.open();
        shlMod.layout();
        Display display = getParent().getDisplay();
        while (!shlMod.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shlMod = new Shell(getParent(), getStyle());
        shlMod.setSize(800, 300);
        shlMod.setText("Modのインストール");
        shlMod.setLayout(new FormLayout());

        Composite mainComposite = new Composite(shlMod, SWT.NONE);
        FormData fd_mainComposite = new FormData();
        fd_mainComposite.bottom = new FormAttachment(0, 300);
        fd_mainComposite.right = new FormAttachment(0, 400);
        fd_mainComposite.top = new FormAttachment(0);
        fd_mainComposite.left = new FormAttachment(0);
        mainComposite.setLayoutData(fd_mainComposite);
        mainComposite.setLayout(new BorderLayout(0, 0));

        Composite topComposite = new Composite(mainComposite, SWT.NONE);
        topComposite.setLayoutData(BorderLayout.NORTH);
        FillLayout fl_topComposite = new FillLayout(SWT.VERTICAL);
        fl_topComposite.marginWidth = 5;
        fl_topComposite.marginHeight = 3;
        topComposite.setLayout(fl_topComposite);

        Label label = new Label(topComposite, SWT.SHADOW_NONE);
        label.setAlignment(SWT.LEFT);
        label.setText("Mod名");

        modNameText = new Text(topComposite, SWT.BORDER);

        Composite bottomComposite = new Composite(mainComposite, SWT.NONE);
        bottomComposite.setLayoutData(BorderLayout.SOUTH);
        bottomComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        messageLabel = new CLabel(bottomComposite, SWT.CENTER);
        messageLabel.setText("");

        Composite buttonComposite = new Composite(bottomComposite, SWT.NONE);
        RowLayout rl_buttonComposite = new RowLayout(SWT.HORIZONTAL);
        rl_buttonComposite.spacing = 0;
        rl_buttonComposite.justify = true;
        buttonComposite.setLayout(rl_buttonComposite);

        Button okButton = new Button(buttonComposite, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!fileManager.findPak(file).isEmpty()) {
                    result = new Result(file, modNameText.getText());
                    shlMod.close();
                }
            }
        });

        Button cancelButton = new Button(buttonComposite, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                shlMod.close();
            }
        });

        fileTree = new Tree(mainComposite, SWT.BORDER);
        fileTree.setLayoutData(BorderLayout.CENTER);
        fileTree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (e.item instanceof FileTreeItem) {
                    final FileTreeItem item = (FileTreeItem) e.item;
                    final File selectFile = item.getFile();
                    if (item.isRoot()) {
                        final File parent = selectFile.getParentFile();
                        if (!parent.equals(fileManager.getTemp())) {
                            refreshContents(parent);
                        }
                    } else {
                        if (selectFile.isDirectory()) {
                            refreshContents(selectFile);
                        }
                    }
                }
            }
        });

        Composite imageComposite = new Composite(shlMod, SWT.NONE);
        imageComposite.setLayout(null);
        FormData fd_imageComposite = new FormData();
        fd_imageComposite.bottom = new FormAttachment(0, 300);
        fd_imageComposite.right = new FormAttachment(0, 801);
        fd_imageComposite.top = new FormAttachment(0);
        fd_imageComposite.left = new FormAttachment(0, 401);
        imageComposite.setLayoutData(fd_imageComposite);

        imageStackComposite = new StackImageComposite(imageComposite);
        imageStackComposite.setBounds(20, 0, 360, 300);

        Button leftButton = new Button(imageComposite, SWT.NONE);
        leftButton.setBounds(0, 0, 20, 300);
        leftButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                imageStackComposite.back();
            }
        });
        leftButton.setText("<");

        Button rightButton = new Button(imageComposite, SWT.NONE);
        rightButton.setBounds(380, 0, 20, 300);
        rightButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                imageStackComposite.next();
            }
        });
        rightButton.setText(">");
        refreshContents(file);
    }

    private void refreshContents(File file) {
        this.file = file;
        imageStackComposite.removeAll();
        fileTree.removeAll();
        modNameText.setText(file.getName());
        final FileTreeItem root = new FileTreeItem(fileTree, file);
        final Set<Pak> pak = fileManager.findPak(file);
        if (pak.isEmpty()) {
            messageLabel.setText("最上位にゲームデータがありません");
            messageLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
        } else {
            messageLabel.setText("インストール可能です");
            messageLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
        }
        for (final File f : Objects.requireNonNull(file.listFiles())) {
            file(root, f);
            if (f.isFile()) {
                final String name = f.getName();
                if (name.contains(".")) {
                    switch (name.substring(name.lastIndexOf(".")).toLowerCase()) {
                        case ".jpg":
                        case ".png":
                            final Label label = new Label(imageStackComposite, SWT.NONE);
                            try (FileInputStream stream = new FileInputStream(f)) {
                                final ImageData imageData = new Image(Display.getDefault(), stream).getImageData();
                                final int height = 340 * imageData.height / imageData.width;
                                label.setImage(new Image(Display.getDefault(), imageData.scaledTo(340, height)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default: // ignored
                            break;
                    }
                }
            }
        }
        if (imageStackComposite.getChildren().length > 0) {
            shlMod.setSize(800, 300);
            imageStackComposite.setTopControl(imageStackComposite.getChildren()[0]);
        } else {
            shlMod.setSize(400, 300);
        }
        shlMod.layout();
        root.setExpanded(true);
        fileTree.redraw();
    }

    private void file(FileTreeItem item, File file) {
        final FileTreeItem i = new FileTreeItem(item, file);
        if (file.isDirectory()) {
            for (final File f : Objects.requireNonNull(file.listFiles())) {
                file(i, f);
            }
        }
    }

    public static class Result {
        private final File file;
        private final String title;

        public Result(File file, String title) {
            this.file = file;
            this.title = title;
        }

        public File getFile() {
            return file;
        }

        public String getTitle() {
            return title;
        }
    }

}
