package com.chemistry.enotebook.experiment.businessmodel.stoichiometry;

import com.chemistry.enotebook.domain.StoicModelInterface;

import java.util.Comparator;

public class ComparatorStoicAdditionOrder implements Comparator<StoicModelInterface> {

    public ComparatorStoicAdditionOrder() {
        super();
    }

    public int compare(StoicModelInterface o1, StoicModelInterface o2) {
        int result = 0;
        if (o1 != null) {
            result = 1;
            if (o2 != null) {
                result = o1.getStoicTransactionOrder() - o2.getStoicTransactionOrder();
            }
        } else if (o2 != null)
            result = -1;

        return result;
    }
}
