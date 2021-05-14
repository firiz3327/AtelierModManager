package net.firiz.ateliermodmanager.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class StackImageComposite extends Composite {

    private final StackLayout layout;
    private int index;

    public StackImageComposite(Composite parent) {
        super(parent, SWT.NONE);
        this.layout = new StackLayout();
        setLayout(layout);
    }

    public void setTopControl(Control control) {
        layout.topControl = control;
        layout();
    }

    public void removeAll() {
        index = 0;
        for (final Control control : getChildren()) {
            if (!control.isDisposed()) {
                control.dispose();
            }
        }
    }

    public void back() {
        index--;
        if (0 > index) {
            index = getChildren().length - 1;
        }
        setTopControl(getChildren()[index]);
    }

    public void next() {
        index++;
        if (index >= getChildren().length) {
            index = 0;
        }
        setTopControl(getChildren()[index]);
    }

    @Override
    protected void checkSubclass() {
        // ignored
    }

}
