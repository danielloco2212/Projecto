package com.example.pagebuilder;

import java.awt.*;
import javax.swing.*;

/**
 * Subclase de FlowLayout que soporta completamente el ajuste de línea de componentes.
 */
public class WrapLayout extends FlowLayout {
    public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return calcularTamanoDisposicion(target, true); // Calcula el tamaño preferido de la disposición
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimo = calcularTamanoDisposicion(target, false); // Calcula el tamaño mínimo de la disposición
        minimo.width -= (getHgap() + 1); // Ajusta el ancho mínimo
        return minimo;
    }

    private Dimension calcularTamanoDisposicion(Container target, boolean preferido) { // Renombrado
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;

            if (targetWidth == 0)
                targetWidth = Integer.MAX_VALUE;

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            Dimension dimension = new Dimension(0, 0); 
            int anchoFila = 0; 
            int altoFila = 0; 

            int numMiembros = target.getComponentCount(); // Número de componentes

            for (int i = 0; i < numMiembros; i++) {
                Component componente = target.getComponent(i); // Componente actual

                if (componente.isVisible()) {
                    Dimension d = preferido ? componente.getPreferredSize() : componente.getMinimumSize(); // Obtiene el tamaño preferido o mínimo

                    if (anchoFila + d.width > maxWidth) { // Si el componente no cabe en la fila actual
                        agregarFila(dimension, anchoFila, altoFila); // Añade la fila actual a la dimensión total
                        anchoFila = 0; // Reinicia el ancho de la fila
                        altoFila = 0; // Reinicia el alto de la fila
                    }

                    if (anchoFila > 0) { // Si no es el primer componente de la fila, añade el espacio horizontal
                        anchoFila += hgap;
                    }

                    anchoFila += d.width; // Añade el ancho del componente al ancho de la fila
                    altoFila = Math.max(altoFila, d.height); // Actualiza el alto máximo de la fila
                }
            }

            agregarFila(dimension, anchoFila, altoFila); 

            dimension.width += horizontalInsetsAndGap; // Añade los insets y el espacio horizontal
            dimension.height += insets.top + insets.bottom + vgap * 2; // Añade los insets y el espacio vertical

            
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dimension.width -= (hgap + 1); // Reduce el ancho para evitar la barra de desplazamiento horizontal
            }

            return dimension;
        }
    }

    private void agregarFila(Dimension dimension, int anchoFila, int altoFila) { 
        dimension.width = Math.max(dimension.width, anchoFila); // Actualiza el ancho máximo de la disposición

        if (dimension.height > 0) { // Si no es la primera fila, añade el espacio vertical
            dimension.height += getVgap();
        }

        dimension.height += altoFila; // Añade el alto de la fila a la altura total
    }
}