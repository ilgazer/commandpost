package com.ilgazer.commandpost.ui;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import net.java.games.input.Controller;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class RC_Config {
    private JComboBox<ControllerItem> controllerComboBox;
    private JComboBox comboBox1;
    private JLabel componentType;

    public RC_Config() {
        controllerComboBox.addActionListener(actionEvent -> {
        });
        SwingObservable.itemSelection(controllerComboBox)
                .filter(event -> event.getStateChange() == ItemEvent.SELECTED)
                .map(ItemEvent::getItem)
                .cast(ControllerItem.class)
                .subscribe();
    }

    private static class ControllerItem {
        final Controller controller;

        private ControllerItem(Controller controller) {
            this.controller = controller;
        }

        @Override
        public String toString() {
            return controller.getName();
        }
    }
}
