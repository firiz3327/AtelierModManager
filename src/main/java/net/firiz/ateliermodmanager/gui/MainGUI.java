package net.firiz.ateliermodmanager.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.firiz.ateliermodmanager.json.ModFileElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;

import net.firiz.ateliermodmanager.file.FileManager;
import net.firiz.ateliermodmanager.json.ModsJson;
import swing2swt.layout.BorderLayout;

public class MainGUI {

    private static final FileManager fileManager = FileManager.INSTANCE;
    private final ModsJson mods;
    private Table table;
    private CLabel gameName;

    public MainGUI() {
        this.mods = fileManager.getModsJson();
    }

    /**
     * Open the window.
     *
     * @wbp.parser.entryPoint
     */
    public void open() {
        Display display = Display.getDefault();
        Shell shlMod = new Shell();
        shlMod.setSize(800, 500);
        shlMod.setText("Atelier Mod Manager");
        shlMod.setLayout(new FillLayout(SWT.HORIZONTAL));

        SashForm sashForm = new SashForm(shlMod, SWT.NONE);
        sashForm.setSashWidth(0);

        ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        table = new Table(scrolledComposite, SWT.CHECK | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        for (final ModTableItem.Column column : ModTableItem.Column.values()) {
            final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(column.getName());
            tableColumn.setWidth(column.getWidth());
        }
        scrolledComposite.setContent(table);
        scrolledComposite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        Composite composite = new Composite(sashForm, SWT.NONE);
        composite.setLayout(new BorderLayout(0, 0));

        Composite composite_1 = new Composite(composite, SWT.NONE);
        composite_1.setLayoutData(BorderLayout.SOUTH);
        composite_1.setLayout(new BorderLayout(0, 0));
        sashForm.setWeights(new int[]{420, 161});

        gameName = new CLabel(composite_1, SWT.NONE);
        gameName.setLayoutData(BorderLayout.CENTER);
        gameName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        gameName.setText("Atelier_Ryza_2.exe");

        Button startButton = new Button(composite_1, SWT.NONE);
        startButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fileManager.startGame();
            }
        });
        startButton.setLayoutData(BorderLayout.EAST);
        startButton.setText(">");

        Button addModButton = new Button(composite, SWT.NONE);
        addModButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final FileDialog dialog = new FileDialog(shlMod, SWT.OPEN);
                dialog.setFilterExtensions(new String[]{"*.zip;*.7z"});
                final String archivePath = dialog.open();
                if (archivePath != null) {
                    final File unarchive = fileManager.unarchive(new File(archivePath));
                    final SelectFolderDialog.Result result = new SelectFolderDialog(shlMod, unarchive).open();
                    if (result != null) {
                        fileManager.getModsJson().addFile(result.getFile(), result.getTitle());
                        refreshContents();
                    }
                }
            }
        });
        addModButton.setLayoutData(BorderLayout.NORTH);
        addModButton.setText("アーカイブからModのインストール");

        try {
            final URL url = new URL(
                    "https://cdn.cloudflare.steamstatic.com/steamcommunity/public/images/apps/1257290/925caa62fcfb2292162c93cea9388c202df52736.jpg");
            try (final InputStream stream = url.openStream()) {
                gameName.setImage(new Image(Display.getDefault(), stream));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                final TableItem item = table.getItem(new Point(e.x, e.y));
                if (e.button == 3) {
                    if (item == null) {
                        // 範囲外がクリックされた
                        System.out.println("範囲外");
                    } else {
                        System.out.println(item); // 右クリック
                    }
                }
            }
        });
        table.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.item instanceof ModTableItem) {
                    final ModTableItem item = (ModTableItem) e.item;
                    if (e.detail == SWT.CHECK) {
                        final ModFileElement mod = item.getMod();
                        mod.setActive(item.getChecked());
                        table.setEnabled(false);
                        fileManager.overwrite(mod);
                        fileManager.getModsJson().save();
                        table.setEnabled(true);
                    } else {
                        for (final TableItem tableItem : table.getItems()) {
                            if (tableItem instanceof ModTableItem) {
                                ((ModTableItem) tableItem).resetSelectColor();
                            }
                        }
                        item.conflictingFileSelect();
                        table.redraw();
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // ダブルクリック
            }
        });

        refreshContents();

        shlMod.open();
        shlMod.layout();
        while (!shlMod.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    public void refreshContents() {
        table.removeAll();
        mods.getFileElements().forEach(mod -> new ModTableItem(table, mod));
    }
}
