package com.chemistry.enotebook.domain;

import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReactionStepModel extends CeNAbstractModel {

    private static final long serialVersionUID = 3012398806567346676L;
    private final ArrayList<BatchesList<ProductBatchModel>> productBatchesList = new ArrayList<>();
    // This is ArrayList of BatchesList objects( Monomers BatchesList for A , Monomers BatchesList for B so on)
    // List holds reagents and solvents Batches added from the Stoic
    private BatchesList<MonomerBatchModel> stoicBatchesList = null;

    public ReactionStepModel() {
        stoicBatchesList = new BatchesList<>();
        productBatchesList.add(new BatchesList<>());
        stoicBatchesList.setPosition(CeNConstants.STOIC_POSITION_CONSTANT);
    }

    public void setProducts(final ArrayList<BatchesList<ProductBatchModel>> products) {
        this.productBatchesList.clear();

        if (products != null && products.size() > 0) {
            for (BatchesList<ProductBatchModel> batchesList : products) {
                boolean found = false;

                for (BatchesList<ProductBatchModel> batchesListProduct : productBatchesList) {
                    if (batchesListProduct != null && batchesListProduct.equals(batchesList)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    productBatchesList.add(batchesList);
                }
            }
        }

        setModified(true);
    }

    public String toString() {
        int stepNumber = 0;
        return "Step " + stepNumber;
    }

    /*
     * @return List of MonomerBatchModels (reagents and solvents)
     */
    private BatchesList<MonomerBatchModel> getStoicBatchesList() {
        if (stoicBatchesList == null) {
            stoicBatchesList = new BatchesList<>();
            stoicBatchesList.setPosition("STE");
        }
        return this.stoicBatchesList;
    }

    public void setStoicBatchesList(BatchesList<MonomerBatchModel> listofBatches) {
        if (listofBatches != null) {
            this.stoicBatchesList = listofBatches;
            setModified(true);
        }

    }

    /**
     * @return List containing Reagents and Solvents from StoicBatchesList
     */
    public List<MonomerBatchModel> getBatchesFromStoicBatchesList() {
        return this.getStoicBatchesList().getBatchModels();
    }

    /*
     * @return ArrayList of StoicModelInterface Objects( Monomers and Reagents,Solvents etc )
     */
    private ArrayList<StoicModelInterface> getStoicModelList() {
        ArrayList<StoicModelInterface> stoicList = new ArrayList<>();
        // adding MonomerList Objects
//		stoicList.addAll(this.monomerBatchesLists);
        // adding MonomerBatchModel objects
        //Filter solvent batches associated with a Reagent/Reactant directly
//		List<MonomerBatchModel> allStoicElementBatches = this.getBatchesFromStoicBatchesList();
        stoicList.addAll(this.getBatchesFromStoicBatchesList());
//		int size = allStoicElementBatches.size();
//		if (size > 0) {
//			List<StoicModelInterface> newList = new ArrayList<>();
//			for (MonomerBatchModel allStoicElementBatche : allStoicElementBatches) {
//				if (allStoicElementBatche.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
//					String key = allStoicElementBatche.getGUIDKey();
//					if (!isSolventForASpecificReacant(key)) {
//						newList.add(allStoicElementBatche);
//					}
//
//				} else {
//					newList.add(allStoicElementBatche);
//				}
//			}
//			stoicList.addAll(newList);
//		}
        return stoicList;
    }

    public List<StoicModelInterface> getStoicElementListInTransactionOrder() {
        return this.getStoicModelList();
    }

    //This is a redundant method to getAllProductBatchModelsInThisStep()
    public List<ProductBatchModel> getProductBatches() {
        List<ProductBatchModel> list = new ArrayList<>();
        if (productBatchesList.size() > 0) {
            for (BatchesList<ProductBatchModel> bl : productBatchesList) {
                list.addAll(bl.getBatchModels());
            }
        }
        return list;
    }

    public List<ProductBatchModel> getIntendedProductBatches() {
        List<ProductBatchModel> list = new ArrayList<>();
        List<ProductBatchModel> productBatchModels = getProductBatches();
        list.addAll(productBatchModels.stream()
                .filter(productBatchModel -> productBatchModel.getBatchType() == BatchType.INTENDED_PRODUCT)
                .collect(Collectors.toList()));
        return list;
    }

    public List<ProductBatchModel> getActualProductBatches() {
        List<ProductBatchModel> list = new ArrayList<>();
        List<ProductBatchModel> productBatchModels = getProductBatches();
        list.addAll(productBatchModels.stream()
                .filter(productBatchModel -> productBatchModel.getBatchType() == BatchType.ACTUAL_PRODUCT)
                .collect(Collectors.toList()));
        return list;
    }
}