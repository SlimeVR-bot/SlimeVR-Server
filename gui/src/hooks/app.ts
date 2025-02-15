import {
  createContext,
  Dispatch,
  Reducer,
  useContext,
  useEffect,
  useLayoutEffect,
  useMemo,
  useReducer,
} from 'react';
import { useNavigate } from 'react-router-dom';
import {
  DataFeedMessage,
  DataFeedUpdateT,
  DeviceDataT,
  StartDataFeedT,
  TrackerDataT,
} from 'solarxr-protocol';
import { useConfig } from './config';
import { useDataFeedConfig } from './datafeed-config';
import { useWebsocketAPI } from './websocket-api';

export interface FlatDeviceTracker {
  device?: DeviceDataT;
  tracker: TrackerDataT;
}

type AppStateAction = { type: 'datafeed'; value: DataFeedUpdateT };

export interface AppState {
  datafeed?: DataFeedUpdateT;
}

export interface AppContext {
  state: AppState;
  trackers: FlatDeviceTracker[];
  dispatch: Dispatch<AppStateAction>;
}

export function reducer(state: AppState, action: AppStateAction) {
  switch (action.type) {
    case 'datafeed':
      return { ...state, datafeed: action.value };
    default:
      throw new Error(`unhandled state action ${action.type}`);
  }
}

export function useProvideAppContext(): AppContext {
  const { sendDataFeedPacket, useDataFeedPacket, isConnected } =
    useWebsocketAPI();
  const { config } = useConfig();
  const { dataFeedConfig } = useDataFeedConfig();
  const navigate = useNavigate();
  const [state, dispatch] = useReducer<Reducer<AppState, AppStateAction>>(
    reducer,
    {
      datafeed: new DataFeedUpdateT(),
    }
  );

  useEffect(() => {
    if (isConnected) {
      const startDataFeed = new StartDataFeedT();
      startDataFeed.dataFeeds = [dataFeedConfig];
      sendDataFeedPacket(DataFeedMessage.StartDataFeed, startDataFeed);
    }
  }, [isConnected]);

  useLayoutEffect(() => {
    if (config && !config.doneOnboarding) {
      navigate('/onboarding/home');
    }
  }, [config]);

  const trackers = useMemo(
    () =>
      (state.datafeed?.devices || []).reduce<FlatDeviceTracker[]>(
        (curr, device) => [
          ...curr,
          ...device.trackers.map((tracker) => ({ tracker, device })),
        ],
        []
      ),
    [state]
  );

  useDataFeedPacket(
    DataFeedMessage.DataFeedUpdate,
    (packet: DataFeedUpdateT) => {
      dispatch({ type: 'datafeed', value: packet });
    }
  );

  return {
    state,
    trackers,
    dispatch,
  };
}

export const AppContextC = createContext<AppContext>(undefined as any);

export function useAppContext() {
  const context = useContext<AppContext>(AppContextC);
  if (!context) {
    throw new Error('useAppContext must be within a AppContext Provider');
  }
  return context;
}
