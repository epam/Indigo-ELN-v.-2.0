
import React from 'react';
import ReactDOM from 'react-dom';
import { Editor } from 'ketcher-react';
import { StandaloneStructServiceProvider } from 'ketcher-standalone';
import { Ketcher } from 'ketcher-core';

const structServiceProvider = new StandaloneStructServiceProvider();
const errorHandler = (error: string) => {
  console.log('[Ketcher Error]:', error);
};

export const renderKetcherEditor = (
  element: HTMLElement,
  props: {
    initialValue?: string;
    onStructureChange?: (struct: string) => void;
  } = {}
) => {
  ReactDOM.render(
    <Editor
      staticResourcesUrl="/public/assets/ketcher"
      structServiceProvider={structServiceProvider}
      errorHandler={errorHandler}
      onInit={(ketcher: Ketcher) => {
        (window as any).ketcher = ketcher;
        if (props.initialValue) {
          ketcher.setMolecule(props.initialValue);
        }

        ketcher.editor.subscribe('change', async () => {
          if (props.onStructureChange) {
            const struct = await ketcher.getMolfile();
            props.onStructureChange(struct);
          }
        });

        window.parent.postMessage(
          {
            eventType: 'init',
          },
          '*'
        );
      }}
    />,
    element
  );
};

export const unmountketcherEditor = (element: HTMLElement) => {
  ReactDOM.unmountComponentAtNode(element);
};
