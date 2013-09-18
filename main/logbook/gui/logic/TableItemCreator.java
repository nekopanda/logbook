/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.logic;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * テーブルの行を作成するインターフェイスです
 *
 */
public interface TableItemCreator {

    TableItem create(Table table, String[] text);
}
